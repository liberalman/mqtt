package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Pair;

import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.cloud.HttpClientConfig;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ExtraService {
    private static final String TAG = ExtraService.class.getSimpleName();
    private static final ExtraService me = new ExtraService();

    ExtraService() {
    }

    public static ExtraService getInstance() {
        return me;
    }

    protected List<Contact> getRobotsFromServer() throws EaseMobException {
        ArrayList var1 = new ArrayList();
        String var2 = HttpClientConfig.getBaseUrlByAppKey() + "/robots";

        try {
            Pair var3 = HttpClient.getInstance().sendRequestWithToken(var2, (String)null, HttpClient.GET);
            if(var3 != null && ((Integer)var3.first).intValue() == 200) {
                String var4 = (String)var3.second;
                JSONObject var5 = new JSONObject(var4);
                if(var5.has("entities")) {
                    JSONArray var6 = var5.getJSONArray("entities");

                    for(int var7 = 0; var7 < var6.length(); ++var7) {
                        JSONObject var8 = var6.getJSONObject(var7);
                        Contact var9 = new Contact();
                        var9.username = var8.getString("username");
                        if(var8.has("name")) {
                            var9.nick = var8.getString("name");
                        }

                        boolean var10 = false;
                        if(var8.has("activated")) {
                            var10 = var8.getBoolean("activated");
                        }

                        if(var10) {
                            var1.add(var9);
                        }
                    }
                }
            } else if(var3 != null) {
                EMLog.e(TAG, "getRobotUsers resp statusCode:" + var3.first);
            } else {
                EMLog.e(TAG, "getRobotUsers resp result is null");
            }

            return var1;
        } catch (EaseMobException var11) {
            throw var11;
        } catch (JSONException var12) {
            throw new EaseMobException(var12.getMessage());
        }
    }
}

