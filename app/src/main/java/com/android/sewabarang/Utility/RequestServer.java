package com.android.sewabarang.Utility;


public class RequestServer {
    // ip localhost untuk avd emulator 10.0.2.2
    private String server_ip = "192.168.1.1";
    private String server_url = "/sewabarang/api/";
    private String img_url = "/sewabarang/assets/img/";

    public String getServer_url(){
        return "http://"+this.server_ip+this.server_url;
    }
    public String getImg_url(){
        return "http://"+this.server_ip+this.img_url;
    }

}
