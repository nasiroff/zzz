package com.webApp.dao;

import com.webApp.model.Blog;
import com.webApp.model.Comment;
import com.webApp.model.User;
import com.webApp.util.CalculateShareTimeToNow;
import com.webApp.util.DbUtil;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class BlogDaoImpl implements BlogDao {
    private final String GET_ALL_BLOGS_AND_USER = "select * from post p inner join user u using (id_user) ";
    private final String GET_BLOG_BY_ID = "select * from post p inner join user u using (id_user) where id_post = ?";
    private final String GET_ALL_BLOGS_AND_COMMENT_COUNT = "select * from post p inner join user u on p.id_user = u.id_user left join comment c on p.id_post = c.id_post order by p.share_date desc";
    private final String UPDATE_VIEW_COUNT = "update post set view_count = view_count + 1 where id_post = ?";
    @Override
    public List<Blog> getAllBlogsAndUser() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Blog> list = new ArrayList<>();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(GET_ALL_BLOGS_AND_USER);
            rs = ps.executeQuery();
            while(rs.next()){
                Blog blog = new Blog();
                blog.setTitle(rs.getString("title"));
                blog.setId(rs.getInt("id_post"));
                blog.setDescription(rs.getString("description"));
                blog.setShareDate(rs.getTimestamp("share_date").toLocalDateTime());
                blog.setTimeDiff(CalculateShareTimeToNow.timeDiff(rs.getTimestamp("share_date").toLocalDateTime()));
                User user = new User();
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setId(rs.getInt("id_user"));
                blog.setUser(user);
                list.add(blog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Blog> getAllBlogsByUser(int idUser) {
        return null;
    }


    @Override
    public boolean createNewBlog(Blog blog) {
        return false;
    }

    @Override
    public Blog getBlogById(int idBlog) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Blog blog = new Blog();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(GET_BLOG_BY_ID);
            ps.setInt(1, idBlog);
            rs = ps.executeQuery();
            if (rs.next()){
                    blog.setId(rs.getInt("id_post"));
                    blog.setTitle(rs.getString("title"));
                    blog.setDescription(rs.getString("description"));
                    blog.setShareDate(rs.getTimestamp("share_date").toLocalDateTime());
                    blog.setViewCount(rs.getInt("view_count"));
                    blog.setTimeDiff(CalculateShareTimeToNow.timeDiff(rs.getTimestamp("share_date").toLocalDateTime()));
                    User user = new User();
                    user.setId(rs.getInt("id_user"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    blog.setUser(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            DbUtil.closeAll(con, ps, rs);
        }
        return blog;
    }

    @Override
    public List<Blog> getAllBlogsAndCommentsCount() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Blog> blogList = new ArrayList<>();
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(GET_ALL_BLOGS_AND_COMMENT_COUNT);
            rs = ps.executeQuery();
            Map<Integer, Blog> blogMap = new LinkedHashMap<>();
            while (rs.next()){
                Blog blog = blogMap.get(rs.getInt("id_post"));
                if (!blogMap.containsKey(rs.getInt("id_post"))){
                    blog = new Blog();
                    blog.setId(rs.getInt("id_post"));
                    blog.setTitle(rs.getString("title"));
                    blog.setDescription(rs.getString("description"));
                    blog.setShareDate(rs.getTimestamp("share_date").toLocalDateTime());
                    blog.setViewCount(rs.getInt("view_count"));
                    blog.setTimeDiff(CalculateShareTimeToNow.timeDiff(rs.getTimestamp("share_date").toLocalDateTime()));
                    User user = new User();
                    user.setId(rs.getInt("id_user"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    blog.setUser(user);
                    blogMap.put(rs.getInt("id_post"), blog);
                }
                if (rs.getInt("id_comment") != 0){
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id_comment"));
                    blog.addComments(comment);
                }
            }
            blogList = new ArrayList<>(blogMap.values());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            DbUtil.closeAll(con, ps, rs);
        }
        return blogList;
    }

    @Override
    public void updateViewCount(int idPost) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DbUtil.getConnection();
            ps = con.prepareStatement(UPDATE_VIEW_COUNT);
            ps.setInt(1, idPost);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            DbUtil.closeAll(con, ps);
        }
    }
}
