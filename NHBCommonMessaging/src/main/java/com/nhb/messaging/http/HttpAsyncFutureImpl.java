package com.nhb.messaging.http;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.PuXmlHelper;

import lombok.AccessLevel;
import lombok.Getter;

class HttpAsyncFutureImpl extends BaseRPCFuture<HttpResponse> implements HttpAsyncFuture {

    @Getter
    private final HttpUriRequest request;

    @Getter
    private final HttpContext context;

    @Getter(AccessLevel.PACKAGE)
    private final FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>() {

        @Override
        public void failed(Exception ex) {
            HttpAsyncFutureImpl.this.failed(ex);
        }

        @Override
        public void completed(HttpResponse result) {
            HttpAsyncFutureImpl.this.setAndDone(result);
        }

        @Override
        public void cancelled() {
            // do nothing.
        }
    };

    HttpAsyncFutureImpl(HttpUriRequest request, HttpContext context) {
        this.request = request;
        this.context = context;
    }

    private void failed(Exception exception) {
        this.setFailedAndDone(exception);
    }

    private PuElement handleResponse(HttpResponse response) {
        PuElement result = null;
        if (response != null) {
            try {
                String responseText = EntityUtils.toString(response.getEntity());
                if (responseText != null) {
                    responseText = responseText.trim();
                    try {
                        if (responseText.startsWith("[")) {
                            result = PuArrayList.fromJSON(responseText);
                        } else if (responseText.startsWith("{")) {
                            result = PuObject.fromJSON(responseText);
                        } else if (responseText.startsWith("<")) {
                            result = PuXmlHelper.parseXml(responseText);
                        } else {
                            result = new PuValue(responseText, PuDataType.STRING);
                        }
                    } catch (Exception ex) {
                        result = new PuValue(responseText, PuDataType.STRING);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while consuming response entity", e);
            }
        }
        return result;
    }

    @Override
    public PuElement getPuElement() throws InterruptedException, ExecutionException {
        return this.handleResponse(this.get());
    }
}
