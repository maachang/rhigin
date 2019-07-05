package rhigin.http;

import java.util.Iterator;
import java.util.Map;

import rhigin.util.ConvertMap;
import rhigin.util.Converter;
import rhigin.util.ListMap;
import rhigin.util.OList;

/**
 * Response.
 */
public class Response implements ConvertMap {
    private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=UTF-8";
    protected int status = 200;
    protected ListMap header = new ListMap();
    protected String ContentType = DEFAULT_CONTENT_TYPE;

    public void clear() {
        header.clear();
        ContentType = DEFAULT_CONTENT_TYPE;
        status = 200;
    }

    public void setStatus(int s) {
        status = s;
    }

    public int getStatus() {
        return status;
    }

    @SuppressWarnings("rawtypes")
    public void setHeader(Map h) {
        if (h == null) {
            return;
        }
        Object k, v;
        Iterator it = h.keySet().iterator();
        while (it.hasNext()) {
            k = it.next();
            if (k == null) {
                continue;
            }
            v = h.get(k);
            if (v == null) {
                continue;
            }
            header.put(k.toString(), v.toString());
        }
    }

    public Object setHeader(Object k, Object v) {
        if (k == null || v == null) {
            return null;
        }
        return header.put(k.toString(), v.toString());
    }

    public Object getHeader(Object k) {
        if (k == null) {
            return null;
        }
        return header.get(k.toString());
    }

    public Object removeHeader(Object k) {
        if (k == null) {
            return null;
        }
        return header.remove(k.toString());
    }

    protected static final String headers(Response h) {
        if (h == null) {
            return "";
        }
        OList<Object[]> raw = h.header.rawData();
        StringBuilder buf = new StringBuilder();
        int len = raw.size();
        if(len > 0) {
            Object[] kv = null;
            for (int i = 0; i < len; i++) {
                kv = (Object[])raw.get(i);
                buf.append(kv[0]).append(": ").append(kv[1]).append("\r\n");
            }
        }
        buf.append("Content-Type: ").append(h.ContentType).append("\r\n");
        return buf.toString();
    }

    /**
     * 取得.
     * 
     * @param key
     *            対象のキーを設定します.
     * @return Object キーに対する要素情報が返却されます.
     */
    @Override
    public Object get(Object key) {
        if ("state".equals(key) || "status".equals(key)) {
            return getStatus();
        } else if("Content-Type".equals(key)) {
            return ContentType;
        }
        return getHeader(key);
    }

    /**
     * 登録.
     * 
     * @param key
     *            対象のキーを設定します.
     * @param value
     *            対象の要素を設定します.
     * @return Object 前回登録されていた内容が返却されます.
     */
    @Override
    public Object put(Object key, Object value) {
        if ("state".equals(key) || "status".equals(key)) {
            int ret = getStatus();
            setStatus(Converter.convertInt(value));
            return ret;
        } else if("Content-Type".equals(key)) {
            String ret = ContentType;
            ContentType = ""+value;
            return ret;
        }
        return setHeader(key, value);
    }

    /**
     * 削除.
     * 
     * @param key
     *            対象のキーを設定します.
     * @return Object キーに対する要素情報が返却されます.
     */
    @Override
    public Object remove(Object key) {
        if ("state".equals(key) || "status".equals(key)) {
            int ret = getStatus();
            setStatus(200);
            return ret;
        } else if("Content-Type".equals(key)) {
            String ret = ContentType;
            ContentType = DEFAULT_CONTENT_TYPE;
            return ret;
        }
        return removeHeader(key);
    }

    /**
     * 存在確認.
     * 
     * @param key
     *            対象のキーを設定します.
     * @return Object キーに対する要素情報が返却されます.
     */
    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        } else if ("state".equals(key) || "status".equals(key) || "Content-Type".equals(key)) {
            return true;
        }
        return header.containsKey(key.toString());
    }

}
