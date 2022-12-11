package com.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.validation.constraints.NotBlank;

public class StoreSalesForm {

	public StoreSalesForm() {}
	public StoreSalesForm(Date start_date, Date end_date) throws ParseException {
		setStart_date(start_date);
		setEnd_date(end_date);
	}
	
	@NotBlank
	private String start_date_str;
	@NotBlank
	private String end_date_str;

	private Date start_date;
	
	private Date end_date;
	// 頁最後の支払情報ID
	private String last_ch;
	// 頁最後の返金情報ID
	private String last_re;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public String getStart_date_str() {
		return start_date_str;
	}
	public void setStart_date_str(String start_date_str) throws ParseException {
		this.start_date_str = start_date_str;
		this.start_date = sdf.parse(start_date_str);
	}
	public String getEnd_date_str() {
		return end_date_str;
	}
	public void setEnd_date_str(String end_date_str) throws ParseException {
		this.end_date_str = end_date_str;
		this.end_date = setDayLastTime(new SimpleDateFormat("yyyy-MM-dd").parse(end_date_str));
		
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
		this.start_date_str = sdf.format(start_date);
	}
	public Date getEnd_date() {
		return end_date;
	}
	public void setEnd_date(Date end_date) throws ParseException {
		this.end_date = setDayLastTime(end_date);
		this.end_date_str = sdf.format(end_date);
	}
	
	/**
	 * 最終時間を設定する
	 */
	private Date setDayLastTime(Date date) throws ParseException {
		
		// localdate変換
		ZoneId timeZone = ZoneId.systemDefault();
		LocalDate ld = date.toInstant().atZone(timeZone).toLocalDate();
		
		LocalDateTime ldt = LocalDateTime.of(ld.getYear(),ld.getMonthValue(),ld.getDayOfMonth(),23,59,59);
		ZonedDateTime zoned = ldt.atZone(ZoneOffset.systemDefault());
		Instant instant = zoned.toInstant();
		
		return Date.from(instant);
	}
	public String getLast_ch() {
		return last_ch;
	}
	public void setLast_ch(String last_ch) {
		this.last_ch = last_ch;
	}
	public String getLast_re() {
		return last_re;
	}
	public void setLast_re(String last_re) {
		this.last_re = last_re;
	}
}
