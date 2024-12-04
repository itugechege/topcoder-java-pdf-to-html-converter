package com.tc.convert.aps.pdf;

import org.fit.pdfdom.resource.Base64Coder;
import org.fit.pdfdom.resource.HtmlResource;
import org.fit.pdfdom.resource.HtmlResourceHandler;

import java.io.IOException;
import java.util.Base64;

public class EmbedAsBase64HandlerCustomized implements HtmlResourceHandler {
    @Override
    public String handleResource(HtmlResource resource) throws IOException {
        String base64Data= null;
        byte[] data = resource.getData();
        if (data != null)
            base64Data = Base64.getEncoder().encodeToString(data);

        return String.format("data:image/PNG;base64,%s", new String(base64Data));
    }
}
