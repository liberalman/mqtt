package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import internal.org.apache.http.entity.mime.HttpMultipartMode;
import internal.org.apache.http.entity.mime.MultipartEntity;

class CustomMultiPartEntity extends MultipartEntity {
    private final CustomMultiPartEntity.ProgressListener a;

    public CustomMultiPartEntity(CustomMultiPartEntity.ProgressListener var1) {
        this.a = var1;
    }

    public CustomMultiPartEntity(HttpMultipartMode var1, CustomMultiPartEntity.ProgressListener var2) {
        super(var1);
        this.a = var2;
    }

    public CustomMultiPartEntity(HttpMultipartMode var1, String var2, Charset var3, CustomMultiPartEntity.ProgressListener var4) {
        super(var1, var2, var3);
        this.a = var4;
    }

    public void writeTo(OutputStream var1) throws IOException {
        super.writeTo(new CustomMultiPartEntity.CountingOutputStream(var1, this.a));
    }

    public static class CountingOutputStream extends FilterOutputStream {
        private final CustomMultiPartEntity.ProgressListener listener;
        private long transferred;

        public CountingOutputStream(OutputStream var1, CustomMultiPartEntity.ProgressListener var2) {
            super(var1);
            this.listener = var2;
            this.transferred = 0L;
        }

        public void write(byte[] var1, int var2, int var3) throws IOException {
            this.out.write(var1, var2, var3);
            this.transferred += (long)var3;
            this.listener.transferred(this.transferred);
        }

        public void write(int var1) throws IOException {
            this.out.write(var1);
            ++this.transferred;
            this.listener.transferred(this.transferred);
        }
    }

    public interface ProgressListener {
        void transferred(long var1);
    }
}

