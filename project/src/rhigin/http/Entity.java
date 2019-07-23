package rhigin.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.Validate.ConditionsChecker;
import rhigin.http.Validate.TypeConvert;

/**
 * Entityコンポーネント.
 * Entityコンポーネントで整形データを生成し、そのデータにしたがって、情報を整形します.
 * <例>
 * Entity.expose("User",
 *  "name",         "string",       "not null",
 *  "age",          "number",       "",
 *  "comments",     "{",            "",
 *    "offset",       "number",       "",
 *    "limit",        "number",       "",
 *    "list",         "$Comment",     "",
 *  "comments",     "}",            ""
 * );
 * 
 * var res = Entity.entity("User", value);
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class Entity {
    /** 定義Entity保存先. **/
    private Map<String,List<EntityColumn>> entityList = new HashMap<String, List<EntityColumn>>();
    
    /** 1つのEntityカラム. **/
    private static final class EntityColumn {
        String name;
        int type;
        String srcType;
        String conditions;
        
        public EntityColumn(String n,String t,String c) {
            name = n;
            srcType = t;
            conditions = c;
            if("{".equals(t)) {
                type = -10;
            } else if("}".equals(t)) {
                type = -11;
            } else if("[".equals(t)) {
                type = -20;
            } else if("]".equals(t)) {
                type = -21;
            } else if(t.startsWith("$")) {
                type = -1;
            } else {
                type = TypeConvert.typeByCode(t);
            }
        }
    }
    
    /**
     * Entity公開データ生成
     * @param name 対象のEntity名を設定します.
     * @param params 設計データ群を設置します.
     *               データ群の設定値は以下の通り.
     * name   type   option
     * "name" string
     */
    public void expose(String name,String... params) {
        EntityColumn n;
        List<EntityColumn> list = new ArrayList<EntityColumn>();
        int len = params.length;
        for(int i = 0; i < len; i+= 3) {
            n = new EntityColumn(params[i+0],params[i+1],params[i+2]);
            list.add(n);
        }
        entityList.put(name,list);
    }
    
    /**
     * Entity公開データにデータ形式をあわせる.
     * @param name 対象のEntity名を設定します.
     * @parma value 元データを設定します.
     * @retrn Object 公開データ形式に変換された内容が返却されます.
     */
    public Object entity(String name,Object value) {
        List<EntityColumn> list = entityList.get(name);
        if(list == null) {
            throw new RhiginException(500, "指定Entity名 " + name + " は存在しません");
        }
        
        return make(list,value);
    }
    
    // make処理.
    private Object make(List<EntityColumn> list,Object value) {
        if(value == null) {
            return null;
        }
        
        // valueがリスト系の場合.
        if(value instanceof List) {
            List array = (List)value;
            int len = array.size();
            List<Object> ret = new ArrayList<Object>();
            for(int i = 0; i < len; i ++) {
                ret.add(make(list,array.get(i)));
            }
            return ret;
        } else if(!(value instanceof Map)) {
            throw new RhiginException(500, "Entity対象の情報がMap形式ではありません");
        }
        
        Object v;
        Object out = new LinkedHashMap<String,Object>();
        LinkedList<Object> buf = new LinkedList<Object>();
        EntityColumn c;
        int len = list.size();
        String[] renameColumn = new String[]{null};
        
        for(int i = 0; i < len; i++) {
            c = list.get(i);
            
            // 特殊指定の場合.
            if(c.type < 0) {
                
                // { or [.
                if(c.type == -10 || c.type == -20) {
                    buf.push(out);
                    Object bef = out;
                    out = (c.type == -10) ?
                            new LinkedHashMap<String,Object>() :
                            new ArrayList<Object>();
                    if(bef instanceof Map) {
                        ((Map) bef).put(c.name,out);
                    } else {
                        ((List) bef).add(out);
                    }
                    
                // } or ].
                } else if(c.type == -11 || c.type == -21) {
                    out = buf.pop();
                    
                // 別のEntity.
                } else if(c.type == -1) {
                    String key = c.srcType.substring(1);
                    List<EntityColumn> nlist = entityList.get(key);
                    if(nlist == null) {
                        throw new RhiginException(500, "指定名 " + key + " のEntity名は存在しません");
                    }
                    
                    Object res = make(nlist,((Map)value).get(c.name));
                    if(out instanceof Map) {
                        ((Map) out).put(c.name,res);
                    } else {
                        ((List) out).add(res);
                    }
                }
                continue;
            }
            
            // 要素変換.
            v = TypeConvert.convert(c.name, c.type, c.srcType,((Map)value).get(c.name));
            
            // データチェック.
            renameColumn[0] = null;
            v = ConditionsChecker.check(renameColumn, c.name, c.srcType, v, c.conditions);
            
            if(out instanceof Map) {
                if(renameColumn[0] != null) {
                    ((Map) out).put(renameColumn[0],v);
                } else {
                    ((Map) out).put(c.name,v);
                }
            } else {
                ((List) out).add(v);
            }
        }
        
        if(buf.size() != 0) {
            throw new RhiginException(500, "括弧の終端が閉じていません");
        }
        
        return out;
    }
}
