package rhigin.util;

import java.util.Locale;

/**
 * OS判別.
 */
public class IsOs {
    
    /**
     * OS判別名 : unix系.
     */
    private static final String OS_NAME_UNIX = "UNIX" ;
    
    /**
     * OS判別名 : dos系.
     */
    private static final String OS_NAME_DOS = "DOS" ;
    
    /**
     * OS判別名 : windowsNT系.
     */
    private static final String OS_NAME_WINNT = "WINDOWS NT" ;
    
    /**
     * OS判別名 : windows9x系.
     */
    private static final String OS_NAME_WIN9X = "WINDOWS 9X" ;
    
    /**
     * OS判別名 : os/2系.
     */
    private static final String OS_NAME_OS_2 = "OS/2" ;
    
    /**
     * OS判別名 : macintosh系.
     */
    private static final String OS_NAME_MACINTOSH = "MACINTOSH" ;
    
    /**
     * OS判別名 : Mac os X以降系.
     */
    private static final String OS_NAME_MAC_OS_X = "MAC-OS-X" ;
    
    /**
     * OS判別名 : netware系.
     */
    private static final String OS_NAME_NETWARE = "NETWARE" ;
    
    /**
     * OS判別名 : tandem系.
     */
    private static final String OS_NAME_TANDEM = "TANDEM" ;
    
    /**
     * OS判別名 : z/os系.
     */
    private static final String OS_NAME_Z_OS = "Z/OS" ;
    
    /**
     * OS判別名 : os/400系.
     */
    private static final String OS_NAME_OS_400 = "OS/400" ;
    
    /**
     * OS判別名 : openvms系.
     */
    private static final String OS_NAME_OPENVMS = "OPENVMS" ;
    
    /**
     * OS判別名 : その他.
     */
    private static final String OS_NAME_UNKNOWN = "UNKNOWN" ;
    
    
    
    /**
     * OS判別コード : unix系.
     */
    public static final int OS_UNIX = 0x00000001 ;
    
    /**
     * OS判別コード : dos系.
     */
    public static final int OS_DOS = 0x00000010 ;
    
    /**
     * OS判別コード : windowsNT系.
     */
    public static final int OS_WINNT = 0x00000021 ;
    
    /**
     * OS判別コード : windows9x系.
     */
    public static final int OS_WIN9X = 0x00000022 ;
    
    /**
     * OS判別コード : os/2系.
     */
    public static final int OS_OS_2 = 0x00000031 ;
    
    /**
     * OS判別コード : macintosh系.
     */
    public static final int OS_MACINTOSH = 0x00000041 ;
    
    /**
     * OS判別コード : Mac os X以降系.
     */
    public static final int OS_MAC_OS_X = 0x00000042 ;
    
    /**
     * OS判別コード : netware系.
     */
    public static final int OS_NETWARE = 0x00000101 ;
    
    /**
     * OS判別コード : tandem系.
     */
    public static final int OS_TANDEM = 0x00000111 ;
    
    /**
     * OS判別コード : z/os系.
     */
    public static final int OS_Z_OS = 0x00000121 ;
    
    /**
     * OS判別コード : os/400系.
     */
    public static final int OS_OS_400 = 0x00000131 ;
    
    /**
     * OS判別コード : openvms系.
     */
    public static final int OS_OPENVMS = 0x00000141 ;
    
    /**
     * OS判別コード : その他.
     */
    public static final int OS_UNKNOWN = 0x0000ffff ;
    
    
    
    /**
     * JavaVM実行Os.
     */
    private final int osType = getVmOS() ;
    
    /**
     * JavaVM実行OSビット.
     */
    private final int osBit = getOsBit() ;
    
    /**
     * シングルトン.
     */
    private static final IsOs SNGL = new IsOs() ;
    
    /**
     * コンストラクタ.
     */
    private IsOs() {
    }
    
    /**
     * シングルトンオブジェクトを取得.
     * @return Os シングルトンオブジェクトが返されます.
     */
    public static final IsOs getInstance() {
        return SNGL ;
    }
    
    
    /**
     * 対象のOS判別コードを取得.
     * @return int OS判別コードが返されます.
     */
    public final int getOS() {
        return osType ;
    }
    
    /**
     * 対象のOSが32Bitか64Bitかを取得.
     * @return int 32Bitの場合は32,64Bitの場合は、64が返されます.
     *             -1が返された場合は不明です.
     */
    public final int getBit() {
        return osBit ;
    }
    
    /**
     * 対象のOS判別コードをOS名に変換.
     * @param type 変換対象のOS判別コードを設定します.
     * @return String 変換されたOS名が返されます.
     */
    public final String getName( int type )
    {
        
        String ret = null ;
        
        switch( type ){
            case OS_UNIX : ret = OS_NAME_UNIX ; break ;
            case OS_DOS : ret = OS_NAME_DOS ; break ;
            case OS_WINNT : ret = OS_NAME_WINNT ; break ;
            case OS_WIN9X : ret = OS_NAME_WIN9X ; break ;
            case OS_OS_2 : ret = OS_NAME_OS_2 ; break ;
            case OS_MACINTOSH : ret = OS_NAME_MACINTOSH ; break ;
            case OS_MAC_OS_X : ret = OS_NAME_MAC_OS_X ; break ;
            case OS_NETWARE : ret = OS_NAME_NETWARE ; break ;
            case OS_TANDEM : ret = OS_NAME_TANDEM ; break ;
            case OS_Z_OS : ret = OS_NAME_Z_OS ; break ;
            case OS_OS_400 : ret = OS_NAME_OS_400 ; break ;
            case OS_OPENVMS : ret = OS_NAME_OPENVMS ; break ;
            default : ret = OS_NAME_UNKNOWN ; break ;
        }
        
        return ret ;
        
    }
    
    /**
     * OS判別処理.
     * @param type 調べるOSの判別コードを設定します.
     * @return boolean 判別結果が返されます.
     */
    public final boolean isOs( int type ) {
        return ( type == osType ) ? true : false ;
    }
    
    /**
     * 実行OS判別の取得.
     * @return int OS判別コードが返されます.
     */
    private static final int getVmOS() {
        
        int ret = OS_UNKNOWN ;
        
        if( checkOS( "windows" ) == true ){
            ret = OS_WINNT ;
        }else if( checkOS( "os2" ) == true ){
            ret = OS_OS_2 ;
        }else if( checkOS("netware" ) == true ){
            ret = OS_NETWARE ;
        }else if( checkOS( "dos" ) == true ){
            ret = OS_DOS ;
        }else if( checkOS( "mac" ) == true ){
            ret = OS_MACINTOSH ;
        }else if( checkOS( "macX" ) == true ){
            ret = OS_MAC_OS_X ;
        }else if( checkOS( "tandem" ) == true ){
            ret = OS_TANDEM ;
        }else if( checkOS( "unix" ) == true ){
            ret = OS_UNIX ;
        }else if( checkOS( "win9x" ) == true ){
            ret = OS_WIN9X ;
        }else if( checkOS( "z/os" ) == true ){
            ret = OS_Z_OS ;
        }else if( checkOS( "os/400" ) == true ){
            ret = OS_OS_400 ;
        }else if( checkOS( "openvms" ) == true ){
            ret = OS_OPENVMS ;
        }
        return ret ;
    }
    
    /**
     * OS判別.
     * @param name チェック対象のOS名を設定します.
     * @return boolean チェック結果が返されます.
     */
    private static final boolean checkOS( String name ) {
        boolean ret = false ;
        String path_sp = null ;
        String os_name = null ;
        
        path_sp = System.getProperty( "path.separator" ) ;
        os_name = System.getProperty( "os.name" ).toLowerCase( Locale.US ) ;
        
        if( name != null ){
            
            if( name.equals( "windows" ) == true ){
                
                ret = os_name.indexOf( "windows" ) > -1 ;
                
            }else if( name.equals( "os/s" ) == true){
                
                ret = os_name.indexOf( "os/2" ) > -1 ;
                
            }else if( name.equals( "netware" ) == true ){
                
                ret= os_name.indexOf( "netware" ) > -1 ;
                
            }else if( name.equals( "dos" ) == true ){
                
                ret = path_sp.equals( ";" ) &&
                    ! checkOS( "netware" ) ;
                
            }else if( name.equals( "macX" ) == true ){
                
                ret = os_name.startsWith( "Mac os" ) ;
                
            }else if( name.equals( "mac" ) == true ){
                
                ret = os_name.indexOf( "mac" ) > -1 ;
                
            }else if( name.equals( "tandem" ) == true ){
                
                ret = os_name.indexOf( "nonstop_kernel" ) > -1 ;
                
            }else if( name.equals( "unix" ) == true ){
                
                ret = path_sp.equals( ":" ) &&
                    ! checkOS( "openvms" ) &&
                    ( 
                        ! checkOS( "mac" ) ||
                        os_name.endsWith( "x" )
                    ) ;
                
            }else if( name.equals( "win9x" ) == true ){
                
                ret = checkOS( "windows" ) &&
                    (
                        os_name.indexOf( "95" ) >= 0 ||
                        os_name.indexOf( "98" ) >= 0 ||
                        os_name.indexOf( "me" ) >= 0 ||
                        os_name.indexOf( "ce" ) >= 0
                    ) ;
                
            }else if( name.equals( "z/os" ) == true ){
                
                ret = os_name.indexOf( "z/os" ) > -1 ||
                    os_name.indexOf( "os/390" ) > -1 ;
                
            }else if( name.equals( "os/400" ) == true ){
                
                ret = os_name.indexOf( "os/400" ) > -1 ;
                
            }else if( name.equals( "openvms" ) == true ){
                
                ret = os_name.indexOf( "openvms" ) > -1 ;
                
            }
        }
        
        return ret ;
    }
    
    /**
     * 32BitOS,64BitOS判別.
     * @return int 32ビットの場合は、32,64ビットの場合は、64が返されます.
     *             -1が返された場合は、不明です.
     */
    private static final int getOsBit() {
        String os = System.getProperty( "sun.arch.data.mode" ) ;
        if( os != null && ( os = os.trim() ).length() > 0 ) {
            if( "32".equals( os ) ) {
                return 32 ;
            }
            else if( "64".equals( os ) ) {
                return 64 ;
            }
        }
        os = System.getProperty( "os.arch" ) ;
        if( os == null || ( os = os.trim() ).length() <= 0 ) {
            return -1 ;
        }
        if( os.endsWith( "32" ) ) {
            return 32 ;
        }
        else if( os.endsWith( "64" ) ) {
            return 64 ;
        }
        return 32 ;
    }
}

