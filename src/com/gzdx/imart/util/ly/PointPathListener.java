package com.gzdx.imart.util.ly;

import java.util.Queue;


/**
 * 
 * @author liuyin
 * @version ����ʱ�䣺Nov 23, 2011 11:09:19 AM
 * 
 *  
 */
public interface PointPathListener {

	/***
	 * �Ѿ�ѡ������·��
	 * @param value : �û�ѡ��ĵ��б�
	 * @return true ��ʾ��ʾ·��, false���·��
	 */
	boolean onPathSelected(Queue<Integer> value);
	
}
