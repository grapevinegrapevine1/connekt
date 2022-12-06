package com.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.form.StoreSalesListForm;
import com.util.SqlUtil;

public class StoreSalesListDao{
	
	public List<StoreSalesListForm> getList(Connection conn, int store_id, Date start_date , Date end_date) throws Exception {
		
		try(PreparedStatement ps = conn.prepareStatement(
				"select"
				+ "    use_date"
				+ "    , so.`name` as name"
				+ "    , so.price as price"
				+ "    , u.`name` as user_name"
				+ "    , u.email as email "
				+ "from"
				+ "    user_option_count_date uocd "
				+ "    inner join user_option_count uoc "
				+ "        on uoc.id = uocd.user_option_count_id "
				+ "    inner join store_option so "
				+ "        on so.id = uoc.option_id "
				+ "    inner join `user` u "
				+ "        on u.id = uoc.user_id "
				+ "where"
				+ "    so.store_id = ? "
				+ "    and use_date between ? and ?"
				+ "")){
			
			// パラメータ設定
			new SqlUtil().setParam(ps, new Object[] {store_id, start_date, end_date});
			
			// SQl実行
			try(ResultSet rs =  ps.executeQuery()) {
				
				List<StoreSalesListForm> list = new ArrayList<StoreSalesListForm>();
				while (rs.next()) {
					// 値設定
					StoreSalesListForm storeSalesListForm = new StoreSalesListForm();
					storeSalesListForm.setEmail(rs.getString("email"));
					storeSalesListForm.setName(rs.getString("name"));
					storeSalesListForm.setPrice(rs.getInt("price"));
					storeSalesListForm.setUse_date(rs.getDate("use_date"));
					storeSalesListForm.setUser_name(rs.getString("user_name"));
					list.add(storeSalesListForm);
				}
				
				return list;
			}
			
		}catch(Exception e){
			throw e;
		}
	}
}
