package com.gzdx.imart.util.ly;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.*;
import android.graphics.Paint;

import android.graphics.Point;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author liuyin
 * @version 创建时间：Nov 23, 2011 11:07:06 AM
 * 
 *          使用方法
 * 
 *          setConnectCrossingPoint(boolean) 是否两点间经过点自动连接，默认不连接
 * 			 
 * 
 *          代码或布局xml创建PointPathView类对象 ,并初始化
 *          <com.gzdx.imart.util.ly.PointPathView
 *          android:id="@+id/pointPathView" android:layout_width="fill_parent"
 *          android:layout_height="wrap_content" />
 * 
 *          例如 PointPathView f_pointPathView = (PointPathView)
 *          findViewById(R.id.pointPathView); f_pointPathView.initial(this);
 *          f_pointPathView.invalidate();
 * 
 * 
 * 
 *          实现接口PointPathListener,当用户选择完路径后会调用 boolean
 *          onPathSelected(Queue<Integer> value) 方法，其中value就是选择的点
 * 
 * 
 *          通常使用逻辑流程
 * 
 *          自己创建PointPathView ->自己初始化并指派PointPathListener ->用户选择完毕自动调用boolean
 *          onPathSelected(Queue<Integer> value)
 *          ->自己调用PointPathView.Disable()禁止用户输入 ->自己代码判断结果是否正确（比如查询数据库）
 *          ->自己调用PointPathView.showCorrect(boolean)显示是否正确
 *          ->自己调用PointPathView.Enable()允许用户再次输入
 * 
 */
public class PointPathView extends View {
	public static final double OUTER_SIZE_RATIO = 0.75;
	public static final double MIDDLE_SIZE_RATIO = 0.90;
	public static final double INNER_SIZE_RATIO = 0.40;

	public static final int SELECTED_COLOR = 0xfff0dd00;

	public static final int UNSELECTED_COLOR = 0xff999999;

	public static final int MANTLE_COLOR = 0xff000000;
	public static final int CORE_COLOR = 0xffffffff;
	public static final int PATH_COLOR = 0xffcccccc;

	// public static final int WAITING_COLOR = 0xff1111ff;

	public static final int CORRECT_COLOR = 0xff4caa14;
	public static final int INCORRECT_COLOR = 0xffdd1111;

	public static final int DEFAULT_GRID_SIZE = 3;

	private ShapeDrawable outerCircles[], middleCircles[], innerCircles[];

	public boolean actionEnable;

	private Paint pathPaint;
	private int gridSize;
	private int gridLength;

	private LinkedList<Point> pathPoints;
	private LinkedList<Integer> pathOrder;

	private Queue<Integer> currentPath;
	// private boolean highlight;

	private int wildX, wildY;
	private int currentColor;

	private int outerDiameter;
	private int middleDiameter;
	private int innerDiameter;

	private int outerOffset;
	private int middleOffset;
	private int innerOffset;

	private boolean connectCrossingPoint = true;

	// private int PointRadius;
	// private int BaseRadius;
	// private int BaseHalfLength;

	private PointPathListener pathListener;

	public PointPathView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		actionEnable = true;

		gridSize = DEFAULT_GRID_SIZE;
		gridLength = 0;

		wildX = -1;
		wildY = -1;

		currentColor = SELECTED_COLOR;

		currentPath = new LinkedList<Integer>();

		pathOrder = new LinkedList<Integer>();
		pathPoints = new LinkedList<Point>();

		pathPaint = new Paint();
		pathPaint.setColor(PATH_COLOR);

		updateDrawableNodes();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!actionEnable)
			return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			currentPath = new LinkedList<Integer>();
			currentColor = SELECTED_COLOR;

			updatePath(currentPath);

		case MotionEvent.ACTION_MOVE:

			float x = event.getX(),
			y = event.getY();
			wildX = (int) x;
			wildY = (int) y;

			int cell_length = gridLength / gridSize;
			int xblocktemp = (int) x / (cell_length);
			if (xblocktemp >= gridSize) {
				xblocktemp = gridSize - 1;
			}

			int yblocktemp = (int) y / (cell_length);
			if (yblocktemp >= gridSize) {
				yblocktemp = gridSize - 1;
			}

			int locus_x = (int) ((((float) xblocktemp) + 0.5) * cell_length);
			int locus_y = (int) ((((float) yblocktemp) + 0.5) * cell_length);
			int locus_dist = (int) Math.sqrt(Math.pow(x - locus_x, 2)
					+ Math.pow(y - locus_y, 2));
			if (locus_dist <= middleDiameter / 2) {
				int nodeNum = (yblocktemp * gridSize) + xblocktemp;
				if (!currentPath.contains(nodeNum)) {
					// 2011 年12月2日 修改 增加 crossConnect功能
					if (connectCrossingPoint) {
						int lastPoint = -1;
						for (Integer i : currentPath) {
							lastPoint = i.intValue();
						}
						
						if(lastPoint>=0)
						{
							if (lastPoint / gridSize == nodeNum / gridSize
									&& java.lang.Math.abs(nodeNum - lastPoint) >= 2
									&& currentPath.size() >= 1) {

								int beginIndex = nodeNum;
								int endIndex = lastPoint;
								if (beginIndex > endIndex) {
									beginIndex = lastPoint;
									endIndex = nodeNum;
								}
								for (int jt = beginIndex + 1; jt < endIndex; jt++) {
									if(!currentPath.contains(jt)) currentPath.offer(jt);
								}

							} else if (lastPoint % gridSize == nodeNum % gridSize
									&& java.lang.Math.abs(nodeNum - lastPoint)
											/ gridSize >= 2
									&& currentPath.size() >= 1) {

								int beginIndex = nodeNum;
								int endIndex = lastPoint;
								if (beginIndex > endIndex) {
									beginIndex = lastPoint;
									endIndex = nodeNum;
								}
								for (int jt = beginIndex + gridSize; jt < endIndex; jt += gridSize) {
									if(!currentPath.contains(jt)) currentPath.offer(jt);
								}
							} else if (gridSize > 2) {

								int bigger = nodeNum;
								int smaller = lastPoint;
								if (nodeNum < lastPoint) {
									bigger = lastPoint;
									smaller = nodeNum;
								}
								
								int x1= smaller % gridSize;
								int y1= smaller / gridSize;							
								int x2= bigger % gridSize;
								int y2= bigger / gridSize;
								
								double a = ((y2-y1)*1.0) / ((x2-x1)*1.0);
								double b= y2-a*x2;
												
								for (int jt = smaller + 1; jt < bigger; jt ++) {
									int jtx3 = jt % gridSize;
									int jty3 = jt / gridSize;
									double y3 = jty3*1.0;
									double x3 = jtx3*1.0;
									if( (y3 == a*x3 + b)   && (!currentPath.contains(jt)) )
									{
										currentPath.offer(jt);
									}
									
								}
								
							}
						}
					}
					currentPath.offer(nodeNum);
					updatePath(currentPath);
					return true;
				}
			}

			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			wildX = -1;
			wildY = -1;
			if (currentPath.size() == 0) {
				return true;
			}

			// currentColor = WAITING_COLOR;

			updatePath(currentPath);

			if (pathListener != null) {
				if (!pathListener.onPathSelected(currentPath)) {
					clearPath();
				}
			}

			break;
		default:
			return false;
		}
		return true;
	}

	public void updateDrawableNodes() {
		outerCircles = new ShapeDrawable[gridSize * gridSize];
		middleCircles = new ShapeDrawable[gridSize * gridSize];
		innerCircles = new ShapeDrawable[gridSize * gridSize];

		for (int i = 0; i < gridSize * gridSize; i++) {
			outerCircles[i] = new ShapeDrawable(new OvalShape());
			outerCircles[i].getPaint().setColor(UNSELECTED_COLOR);
			middleCircles[i] = new ShapeDrawable(new OvalShape());
			middleCircles[i].getPaint().setColor(MANTLE_COLOR);
			innerCircles[i] = new ShapeDrawable(new OvalShape());
			innerCircles[i].getPaint().setColor(CORE_COLOR);
		}

		outerDiameter = (int) ((double) (gridLength / gridSize) * OUTER_SIZE_RATIO);
		middleDiameter = (int) ((double) outerDiameter * MIDDLE_SIZE_RATIO);
		innerDiameter = (int) ((double) outerDiameter * INNER_SIZE_RATIO);

		outerOffset = gridLength / (gridSize * 2) - outerDiameter / 2;
		middleOffset = gridLength / (gridSize * 2) - middleDiameter / 2;
		innerOffset = gridLength / (gridSize * 2) - innerDiameter / 2;

		pathPaint.setStrokeWidth(innerDiameter);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);

		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				int curX = j * gridLength / gridSize, curY = i * gridLength
						/ gridSize;

				// 每个圆有效点判定区域
				outerCircles[gridSize * i + j].setBounds(curX + outerOffset,
						curY + outerOffset, curX + outerOffset + outerDiameter,
						curY + outerOffset + outerDiameter);
				middleCircles[gridSize * i + j].setBounds(curX + middleOffset,
						curY + middleOffset, curX + middleOffset
								+ middleDiameter, curY + middleOffset
								+ middleDiameter);
				innerCircles[gridSize * i + j].setBounds(curX + innerOffset,
						curY + innerOffset, curX + innerOffset + innerDiameter,
						curY + innerOffset + innerDiameter);
			}
		}
	}

	public void refreshPath() {
		updatePath(pathOrder);
	}

	private void updatePath(Queue<Integer> lockPattern) {

		pathPaint.setColor(currentColor);

		pathOrder = new LinkedList<Integer>();
		pathPoints = new LinkedList<Point>();
		Iterator<Integer> pathIterator = lockPattern.iterator();

		for (ShapeDrawable e : outerCircles) {
			e.getPaint().setColor(UNSELECTED_COLOR);
		}

		if (pathIterator.hasNext()) {
			int e = pathIterator.next().intValue();

			outerCircles[e].getPaint().setColor(currentColor);

			pathOrder.offer(new Integer(e));
		}

		while (pathIterator.hasNext()) {
			int e = pathIterator.next().intValue();
			outerCircles[e].getPaint().setColor(currentColor);
			pathOrder.offer(new Integer(e));
		}

		int nodeA = 0, nodeB = 0;
		int startX = 0, startY = 0, endX = 0, endY = 0;
		pathIterator = lockPattern.iterator();

		if (lockPattern.size() > 0) {
			nodeA = pathIterator.next();
			startX = (nodeA % gridSize) * gridLength / gridSize
					+ (gridLength / (gridSize * 2));
			startY = (nodeA / gridSize) * gridLength / gridSize
					+ (gridLength / (gridSize * 2));
			pathPoints.offer(new Point(startX, startY));

			if (pathIterator.hasNext()) {
				nodeB = pathIterator.next();
				endX = (nodeB % gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));
				endY = (nodeB / gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));
				pathPoints.offer(new Point(endX, endY));
			}

			while (pathIterator.hasNext()) {
				nodeA = nodeB;
				nodeB = pathIterator.next();

				startX = (nodeA % gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));
				startY = (nodeA / gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));
				endX = (nodeB % gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));
				endY = (nodeB / gridSize) * gridLength / gridSize
						+ (gridLength / (gridSize * 2));

				pathPoints.offer(new Point(startX, startY));
				pathPoints.offer(new Point(endX, endY));
			}
		}

		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		Point pointA, pointB;
		Iterator<Point> points = pathPoints.iterator();
		// double angle;
		// float centerlineX, centerlineY, pointX, pointY;

		while (points.hasNext()) {
			pointA = points.next();
			if (points.hasNext()) {
				pointB = points.next();

				// angle = Math.atan2(pointA.y-pointB.y,pointA.x-pointB.x);

				canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y,
						pathPaint);
				if (!points.hasNext() && wildX >= 0 && wildY >= 0) {
					canvas
							.drawLine(pointB.x, pointB.y, wildX, wildY,
									pathPaint);
				}
			} else if (wildX >= 0 && wildY >= 0) {
				canvas.drawLine(pointA.x, pointA.y, wildX, wildY, pathPaint);
			}
		}

		for (ShapeDrawable e : outerCircles)
			e.draw(canvas);
		for (ShapeDrawable e : middleCircles)
			e.draw(canvas);
		for (ShapeDrawable e : innerCircles)
			e.draw(canvas);

	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// 计算各圆直径
		outerDiameter = (int) ((double) (gridLength / gridSize) * OUTER_SIZE_RATIO);
		middleDiameter = (int) ((double) outerDiameter * MIDDLE_SIZE_RATIO);
		innerDiameter = (int) ((double) outerDiameter * INNER_SIZE_RATIO);

		// 各圆间隔
		outerOffset = gridLength / (gridSize * 2) - outerDiameter / 2;
		middleOffset = gridLength / (gridSize * 2) - middleDiameter / 2;
		innerOffset = gridLength / (gridSize * 2) - innerDiameter / 2;

		updateDrawableNodes();

		invalidate();
	}

	// 又框架传入view大小
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int value = Math.min(width, height);
		if (width == 0) {
			value = height;
		} else if (height == 0) {
			value = width;
		}
		gridLength = value;
		setMeasuredDimension(value, value);
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int input) {
		gridSize = input;
		updateDrawableNodes();
	}

	public void initial(PointPathListener listener) {
		pathListener = listener;
		updatePath(currentPath);
	}

	public void setPointPathListener(PointPathListener listener) {
		pathListener = listener;
	}

	public void setPath(Queue<Integer> lockPattern) {
		updatePath(lockPattern);
	}

	public void clearPath() {
		this.setPath(new LinkedList<Integer>());
	}

	public boolean isEnable() {
		return actionEnable;
	}

	/**
	 * 设置允许输入
	 * 
	 * @param value
	 *            是否允许
	 */
	public void Enable(boolean value) {
		actionEnable = value;
	}

	/**
	 * 设置允许输入
	 * 
	 * @param value
	 */
	public void Enable() {
		actionEnable = true;
	}

	/**
	 * 设置不允许输入
	 * 
	 * @param value
	 */
	public void Disable() {
		actionEnable = false;
	}

	/***
	 * 显示是否正确
	 * 
	 * @param isCorrect
	 *            是否正确
	 */
	public void showCorrect(boolean isCorrect) {
		if (isCorrect) {
			currentColor = CORRECT_COLOR;
		} else {
			currentColor = INCORRECT_COLOR;
		}
		updatePath(currentPath);
	}

	public boolean isConnectCrossingPoint() {
		return connectCrossingPoint;
	}

	public void setConnectCrossingPoint(boolean connectCrossingPoint) {
		this.connectCrossingPoint = connectCrossingPoint;
	}

}
