package it.polimi.tiw.playlist.controllers;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/Logout")
public class Logout extends HttpServlet{
	private static final long serialVersionUID = 1L;

	public Logout() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Authentication check
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		
		String path = getServletContext().getContextPath() + "/index.html";
		response.sendRedirect(path);
	}
}
