package roti;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class AdminEventHuishou
 */
@WebServlet("/zhs/admin/huishou")
public class AdminEventHuishou extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminEventHuishou() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		___Session s = new ___Session(request);
		s.iniSession();

		___SessionUserStatus sus = new ___SessionUserStatus(request,s);
		request = sus.getUpdateRequest();

		HttpServletRequest r = null;

		___IniDB db = new ___IniDB();
		Connection con = db.getConnection();

		boolean isAdmin = false;

		try{

			String t_uid = s.getUserID();
			String t_name = s.getUsername();
			String t_email = s.getEmail();
			String t_UUID = s.getUUID();
			String t_role = s.getRole();
			//String t_status = s.getStatus();

			String db_email, db_uuid, db_role, db_status;

			PreparedStatement sql_is_admin = con.prepareStatement("select email, role, uuid, status from hunyin.user where email = ? and uuid = ? and role = ? and status = ?");
			sql_is_admin.setString(1, t_email);
			sql_is_admin.setString(2, t_UUID);
			sql_is_admin.setString(3, t_role);
			sql_is_admin.setString(4, "zaixian");

			ResultSet result = sql_is_admin.executeQuery();

			if (result.next()){
				db_role = result.getString("role");
				db_status = result.getString("status");

				if(db_role.equals("guanliyuan") && db_status.equals("zaixian"))
					isAdmin = true;
			}

			if(isAdmin){

				r = getAllArticles(con, request, 1, "a");

				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/zhs/admin/admin_draft_list.jsp");
				dispatcher.forward(r, response);

			}else{
				response.sendRedirect("../login");
			}


		} catch (SQLException e) {
			e.printStackTrace();
			response.sendRedirect("../index");
		} catch(java.lang.NullPointerException e){
			System.out.println("main page");
			e.printStackTrace();
			response.sendRedirect("../index");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.sendRedirect("../index");
		} finally{
			db.setClose();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}



	private HttpServletRequest getAllArticles(Connection con, HttpServletRequest request, int posttype, String genre) throws SQLException{

		String sql_article_genre = "";
		//System.out.println(genre);
		if(genre.equals("a")){
			request.setAttribute("g", "a");
			request.setAttribute("jiele","no");
			sql_article_genre = "select * from post where poststatus = 0 and posttype = 1 ORDER BY PostCreatedTime DESC";
		}else if(genre.equals("wansheng")){
			request.setAttribute("g", "wansheng");
			request.setAttribute("jiele","wansheng");
			sql_article_genre = "select * from post where poststatus = 0 and posttype = 1 and tag = 'wansheng' ORDER BY PostCreatedTime DESC";
		}else{
			request.setAttribute("g", "s");
			request.setAttribute("jiele","no");
			sql_article_genre = "select * from post where genre IN (1,2) and posttype = 1 and poststatus = 0 ORDER BY PostCreatedTime DESC";
		}

		PreparedStatement sql_article = con.prepareStatement(sql_article_genre , ResultSet.TYPE_SCROLL_INSENSITIVE);
		//sql_article.setInt(1, posttype);
		ResultSet rm = sql_article.executeQuery();

		boolean isLast = true;


		List<__Article> articalList = new ArrayList<__Article>();

		if(rm.first()){
			rm.beforeFirst();

			while(isLast) {

				if(rm.next()){

					__Article article = new __Article();
					article.setPostID(rm.getInt("postid"));
					//article.setPostType(posttype);
					article.setTitle(rm.getString("title"));
					//article.setAuthor(rm.getString("author"));
					//article.setEventDate(rm.getString("eventdate"));
					//String[] aed = rm.getString("eventdate").split("-");
					//String disEventDate = aed[1] + "\u6708" + aed[2]+ "\u65E5";
					//article.setDisEventDate(disEventDate);
					//article.setCoverImage(rm.getString("coverimage"));
					//article.setContent(rm.getString("ShortDesc"));
					//article.setPurchaseLink(rm.getString("PurchaseLink"));
					article.setViewCount(rm.getInt("TotalView"));
					article.setCommentCount(rm.getInt("CommentCount"));

					articalList.add(article);

				}else if(rm.last()){
					isLast = false;
				}
			}
		}

		//Gson gson = new Gson();
		//String gsonArticle = gson.toJson(articalList);
		//request.setAttribute("gsonArticle", gsonArticle);

		request.setAttribute("articleList", articalList);

		return request;
	}
}
