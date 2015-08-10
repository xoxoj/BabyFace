package org.faudroids.babyface.server.rest;


import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test")
public class TestResource {

	@Inject
	TestResource() { }

	@GET
	@Path("/foo")
	public String getHelloWorld() {
		return "hello world";
	}

}
