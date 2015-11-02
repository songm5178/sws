

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import server.AbstractRequestHandler;

public class GetHandlerServlet extends AbstractRequestHandler{
	
	@Override
	public HttpResponse doGet(HttpRequest request){
		File file = new File("newFileGetHandlerServlet.txt");
		try {
			PrintWriter out = new PrintWriter("newFileGetHandlerServlet.txt");
			out.write("This is a file from gethandlerservlet");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		HttpResponse response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
		return response;
	}
}
