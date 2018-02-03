package gr.ictpro.jsalatas.gestures.ui.recorddata.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Recorder;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {
    public static final String SERVICE_TYPE = "_http._tcp.local";
    public static final String SERVICE_NAME = "GestureRecorder";
    public static final int PORT = 3000;
    private static HttpServer server = null;

    private static JmDNS jmdns;


    public static void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(3000), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        try {
            jmdns = JmDNS.create();
            ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, PORT, "B");
            jmdns.registerService(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        if(jmdns != null) {
            try {
                jmdns.unregisterAllServices();
                jmdns.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jmdns = null;
        }
    }

    private static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String[] terms = t.getRequestURI().getQuery().split("&");
            Long time = null;
            Integer x = null;
            Integer y = null;
            Integer z = null;
            for (String term : terms) {
                String[] values = term.split("=");
                if (values[0].equalsIgnoreCase("time")) {
                    time = Long.parseLong(values[1]);
                } else if (values[0].equalsIgnoreCase("x")) {
                    x = Integer.parseInt(values[1]);
                } else if (values[0].equalsIgnoreCase("y")) {
                    y = Integer.parseInt(values[1]);
                } else if (values[0].equalsIgnoreCase("z")) {
                    z = Integer.parseInt(values[1]);
                }
            }
            String response = "Not OK";
            if (time != null && x != null && y != null && z != null) {
                Recorder.addPoint(time, x, y, z);
                response = "OK";
            }

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


}
