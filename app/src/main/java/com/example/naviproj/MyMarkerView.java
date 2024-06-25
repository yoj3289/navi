//체중 그래프에서 점 클 시 세부 내용 보여주는 코드
package com.example.naviproj;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private String drawMode;

    public MyMarkerView(Context context, int layoutResource, String mode) {
        super(context, layoutResource);

        // 말풍선에 표시될 뷰 초기화
        tvContent = findViewById(R.id.tvContent);
        drawMode=mode;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Entry 객체가 null이 아니고 터치된 지점이 데이터의 점일 때만 말풍선에 표시
        if (e != null) {
            // 말풍선에 표시할 내용 설정
            if(drawMode=="weight"){
                tvContent.setText("체중\n" + e.getY()+"(Kg)");
            }
            else if(drawMode=="press"){
                tvContent.setText("혈압\n" + e.getY()+"(mmHg)");
            }
            else if(drawMode=="sugar"){
                tvContent.setText("혈당\n" + e.getY()+"(mmHg)");
            }
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // 말풍선이 특정 점 위에 정확히 표시되도록 X, Y 좌표 조정
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}