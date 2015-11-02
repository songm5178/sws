
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import server.AbstractRequestHandler;

public class PostHandlerPlugin extends AbstractRequestHandler {
	@Override
	public HttpResponse doPost(HttpRequest request) {
		File file = new File("newFilePostHandlerServlet.txt");
		try {
			PrintWriter out = new PrintWriter("newFilePostHandlerServlet.txt");
			out.write("This is a file from postHandlerServlet with URI: "
					+ request.getUri().split("/")[2]);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		HttpResponse response = HttpResponseFactory.create200OK(file,
				Protocol.CLOSE);
		return response;
	}
}
