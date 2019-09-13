package com.CarOCR;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ResolutionSet {

	public static float fXpro = 1;
	public static float fYpro = 1;
	public static float fPro = 1;
	public static int nWidth = 800;
	public static int nHeight = 480;

	// 采用饥汉模式创建一个单例(当前类的单例)
	public static ResolutionSet _instance = new ResolutionSet();

	// 当前类的无参数构造方法
	public ResolutionSet() {

	}

	// 设置分辨率的方法，
	public void setResolution(int x, int y) {
		nWidth = x;
		nHeight = y;
		fXpro = (float) x / 800;
		fYpro = (float) y / 480;
		// 使用随机函数类的min方法，来返回两个参数中最负的(最接近负无穷)
		fPro = Math.min(fXpro, fYpro);
	}

	// 迭代子元素视图的方法
	public void iterateChild(View view) {
		// instanceof判断当前视图是否是视图组实例类型
		if (view instanceof ViewGroup) {
			ViewGroup container = (ViewGroup) view;
			// 返回组中子视图的数量
			int nCount = container.getChildCount();
			for (int i = 0; i < nCount; i++) {
				iterateChild(container.getChildAt(i));
			}
		}
		// 更新当前视图view的显示
		UpdateLayout(view);
	}

	void UpdateLayout(View view) {
		// 如何显示视图的容器实例声明
		LayoutParams lp;
		lp = (LayoutParams) view.getLayoutParams();
		if (lp == null)
			return;
		if (lp.width > 0)
			lp.width = (int) (lp.width * fXpro + 0.50001);
		if (lp.height > 0)
			lp.height = (int) (lp.height * fYpro + 0.50001);

		// Padding.....63~67返回此视图的左、右、下、上填充（即距离边框的距离）
		int leftPadding = (int) (fXpro * view.getPaddingLeft());
		int rightPadding = (int) (fXpro * view.getPaddingRight());
		int bottomPadding = (int) (fYpro * view.getPaddingBottom());
		int topPadding = (int) (fYpro * view.getPaddingTop());
		// 给当前显示的视图设置左、右、下、上填充
		view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
		// 每个子页面布局信息，以支持页边距，
		// MarginLayoutParams当前类支持所有子视图属性的列表
		if (lp instanceof ViewGroup.MarginLayoutParams) {

			ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

			mlp.leftMargin = (int) (mlp.leftMargin * fXpro + 0.50001);
			mlp.rightMargin = (int) (mlp.rightMargin * fXpro + 0.50001);
			mlp.topMargin = (int) (mlp.topMargin * fYpro + 0.50001);
			mlp.bottomMargin = (int) (mlp.bottomMargin * fYpro + 0.50001);
		}
		// 判断文字的显示视图
		if (view instanceof TextView) {
			TextView lblView = (TextView) view;
			// 获取默认的TextView的的大小，是以像素为基准
			float txtSize = (float) (fPro * lblView.getTextSize());
			// 给当前控件TextView设置内容字体的显示
			lblView.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
		}
	}
}
