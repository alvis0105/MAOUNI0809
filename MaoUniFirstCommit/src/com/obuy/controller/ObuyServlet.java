package com.obuy.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.commodity_details.model.CdVO;
import com.google.gson.Gson;
import com.obuy.model.ObuyService;
import com.obuy.model.ObuyVO;
import com.pet.model.PetService;
import com.pet.model.PetVO;
import com.shopping_cart.model.ShoppingCartItemVO;
import com.shopping_cart.model.ShoppingCartService;
import com.commodity_details.model.CdVO;
import com.commodity_details.model.CdService;

/**
 * Servlet implementation class ObuyServlet
 */
@WebServlet("/ObuyServlet")
public class ObuyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		if ("GET_ALL_STMT".equals(action)) { 

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

				Integer obuyId = new Integer(req.getParameter("obuyId").trim());
				
				ObuyService oSvc = new ObuyService();
				List<ObuyVO> obuyVO = oSvc.getAll(); 
				
				if (obuyVO == null) {
					errorMsgs.add("沒有這筆資料");
				}
			
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req.getRequestDispatcher("");
					failureView.forward(req, res);
					return;
				}

				
				req.setAttribute("obuyVO", obuyVO); 
				String url = "";
				RequestDispatcher successView = req.getRequestDispatcher(url); 
				successView.forward(req, res);

		}

		//會員一對多個訂單
		if ("findByMemId".equals(action)) { 

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/*************************** 1.接收請求參數 ****************************************/
				Integer oMemId = new Integer(req.getParameter("oMemId").trim());

				/*************************** 2.開始查詢資料 ****************************************/
				ObuyService obuySvc = new ObuyService();
				List<ObuyVO> obuyVO = obuySvc.findByMemId(oMemId);
				HttpSession session = req.getSession();
				session.setAttribute("obuyVO", obuyVO);
				
				
				//這邊svc要叫出來的是在Service時會用哪個方法的名稱，跟dao那邊無關	
				/*************************** 3.查詢完成,準備轉交(Send the Success view) ************/
				req.setAttribute("obuyVO", obuyVO); // 資料庫取出的VO物件,存入req
				String url = "/front-end/member/obuypage.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);// 
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 **********************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得要修改的資料:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/front-end/member/obuypage.jsp");
				failureView.forward(req, res);
			}
		}				
		
		
		if ("GET_ONE_STMT".equals(action)) { 

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				
				Integer obuyId = new Integer(req.getParameter("obuyId").trim());

			
				ObuyService oSvc = new ObuyService();
				ObuyVO obuyVO = oSvc.findByPrimaryKey(obuyId);

				req.setAttribute("obuyVO", obuyVO);
				String url = "/Pet/listonePet.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);// 
				successView.forward(req, res);

			
			} catch (Exception e) {
				errorMsgs.add("無法"+ e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Member/login.jsp");
				failureView.forward(req, res);
			}
		}

		if ("update".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();

			req.setAttribute("errorMsgs", errorMsgs);

			Integer obuyId = new Integer(req.getParameter("obuyId").trim());
			Integer oSurvive = new Integer(req.getParameter("oSurvive").trim());

			ObuyService oSvc = new ObuyService();
			oSvc.update(obuyId,oSurvive);

			res.sendRedirect(req.getContextPath() + "/back-end/obuy/obuyAll.jsp");

		}			
			
			if ("insertWithco".equals(action)) { 

				List<String> errorMsgs = new LinkedList<String>();
				req.setAttribute("errorMsgs", errorMsgs);
				try {
					
					Integer oMemId = new Integer(req.getParameter("oMemId").trim());
					Integer oMoney = new Integer(req.getParameter("oMoney").trim());
					Integer oPaying = new Integer(req.getParameter("oPaying").trim());
					Integer oSend = new Integer(req.getParameter("oSend").trim());
					String obuyOther = req.getParameter("obuyOther").trim();
				

//					取得購物車內訂單明細
					HttpSession session = req.getSession();
					String sessionId = (String) session.getAttribute("sessionId");
					ShoppingCartService shoppingCartSvc = new ShoppingCartService();
					List<ShoppingCartItemVO> cartItems = shoppingCartSvc.getCart(sessionId);
					List<CdVO> list = new ArrayList<CdVO>();

					// 打包訂單明細VO
					for(int i = 0; i < cartItems.size(); i++) {					
						ShoppingCartItemVO cartVO = cartItems.get(i);
						CdVO cdVO = new CdVO();
						Integer cdMoney = cartVO.getCount() * cartVO.getItemPrice();
		
						cdVO.setCdItemId(cartVO.getItemId());
						cdVO.setCdAmount(cartVO.getCount());
						cdVO.setCdMoney(cdMoney);				// 購物車取得為商品單價，存入訂單明細所需為該項商品總額
System.out.println("CdItemId: " + cdVO.getCdItemId());						
System.out.println("CdAmount: " + cdVO.getCdAmount());						
System.out.println("CdMoney: " + cdVO.getCdMoney());						
						list.add(cdVO);
					}
					
					ObuyService oSvc = new ObuyService();

					
					oSvc.insertWithco(oMemId, oMoney, oPaying, oSend,obuyOther,list);


					
				} catch (Exception e) {
					e.printStackTrace();
					errorMsgs.add(e.getMessage());
					
//					String url = "/back-end/Obuy/ObuyAll.jsp";
//					RequestDispatcher failureView = req.getRequestDispatcher(url);
//					failureView.forward(req, res);
				}
			}

		}
	}
