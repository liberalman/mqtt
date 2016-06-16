package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class RestResultParser {
    RestResultParser() {
    }

    static void parseRoom(MultiUserChatRoomModelBase var0, boolean var1, JSONObject var2) throws JSONException {
        String var3 = var2.getString("id");
        String var4 = var2.getString("name");
        var0.setId(var3);
        var0.setName(var4);
        if(var1) {
            if(var2.has("owner")) {
                var0.setOwner(var2.getString("owner"));
            }

            if(var2.has("description")) {
                var0.description = var2.getString("description");
            }

            if(var2.has("maxusers")) {
                var0.maxUsers = var2.getInt("maxusers");
            }

            if(var2.has("affiliations_count")) {
                var0.affiliationsCount = var2.getInt("affiliations_count");
            }

            if(var2.has("affiliations")) {
                ArrayList var5 = new ArrayList();
                JSONArray var6 = var2.getJSONArray("affiliations");

                for(int var7 = 0; var7 < var6.length(); ++var7) {
                    JSONObject var8 = var6.getJSONObject(var7);
                    if(var8.has("owner")) {
                        var0.setOwner(var8.getString("owner"));
                        var5.add(var8.getString("owner"));
                    } else if(var8.has("member")) {
                        var5.add(var8.getString("member"));
                    }
                }

                var0.setMembers(var5);
            }
        }

    }
}
