package com.gzdx.imart.util.ly;

import java.util.Queue;


/**
 * 
 * @author liuyin
 * @version 创建时间：Nov 23, 2011 11:09:19 AM
 * 
 *  
 */
public interface PointPathListener {

	/***
	 * 已经选择完了路径
	 * @param value : 用户选择的点列表
	 * @return true 表示显示路径, false清除路径
	 */
	boolean onPathSelected(Queue<Integer> value);
	
}
