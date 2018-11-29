package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

public class HTMLTextView extends AppCompatTextView implements Html.ImageGetter {
    public HTMLTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param source HTML 형식의 문자열
     */
    public void setHtmlText(String source) {
        Spanned spanned = Html.fromHtml(source, this, null);    // Html.ImageGetter 를 여기다 구현해놨다.
        spanned = deleteWhiteSpace(spanned);
        this.setText(spanned);
    }

    // 문장 마지막에 endLine 공백이 있다면 제거하는 메소드
    private Spanned deleteWhiteSpace(Spanned sp) {
        if (sp.charAt(sp.length() - 1) == '\n')
            return (Spanned) sp.subSequence(0, sp.length() - 2);
        return sp;
    }

    /**
     * Html.ImageGetter 구현.
     *
     * @param source <img> 태그의 주소가 넘어온다.
     * @return 일단 LevelListDrawable 을 넘겨줘서 placeholder 처럼 보여주고, AsyncTask 를 이용해서 이미지를 다운로드
     */
    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable list_d = new LevelListDrawable();

        // 이미지 다운로드 전, 임시 이미지 출력
        Drawable loading = ContextCompat.getDrawable(getContext(), R.drawable.loading);
        list_d.addLevel(0, 0, loading);
        if (loading != null)
            list_d.setBounds(0, 0, loading.getIntrinsicWidth(), loading.getIntrinsicHeight());

        new LoadImage(this).execute(source, list_d);
        return list_d;
    }

    /**
     * 실제 온라인에서 이미지를 다운로드 받을 AsyncTask
     */
    static class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;
        private WeakReference<HTMLTextView> mWeakRefView;

        LoadImage(HTMLTextView view) {
            this.mWeakRefView = new WeakReference<>(view);
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];

            // OKKY 내부 서버저장 파일인 경우 경로를 https:// 로 바꿔준다
            if (source != null && source.charAt(0) == '/' && source.charAt(1) == '/')
                source = source.replace("//", "https://");

            // Bitmap 을 생성하고 관리하는 BitmapFactory 는 Decode 메소드 decodeFile, decodeResouce, decodeStream 가 존재한다.
            // 이때, 원본이 큰 이미지를 그대로 decode 하게 되면 메모리를 왕창 먹어서 앱이 죽을 수 있다. (OutOfMemory : OOM)
            // 따라서 decode 하는 시점에서 이미지를 원하는 만큼 줄여서 불러오는 것이 좋다.
            try {
                InputStream is = new URL(source).openStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565; // 색상 구성표를 변경하여 메모리 사용 감소
//                options.inSampleSize = 1; // 표시되는 이미지 리사이징을 통해서 메모리 사용 감소 (주의 : 로드되는 데이터 자체는 차이없음. 단지 표시되는 사이즈만 줄어듦)
                return BitmapFactory.decodeStream(is, null, options);

                /*
                 * 4096x4096 사이즈 이상의 이미지는 너무 커서 OpenGL을 통한 텍스쳐로 로드되지 않는다.
                 * 그렇기 때문에 이미지를 리사이즈 해야하는데, 이 경우 이미지의 화질이 하락할 가능성이 있다.
                 *
                 * 반면에 Manifest 에서 OpenGL 의 하드웨어 가속을 꺼두면 4096 사이즈 이상의 이미지도 로드할 수 있다.
                 * 이 경우, 사용자의 데이터 소모량 증가 및 퍼포먼스 하락이 발생할 수 있다. (현재는 이 방법 사용)
                 * */
//                return resizeBitmapImage(bitmap, 4096);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("에러", "파일못찾음");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("에러", "경로주소잘못됨 : " + source);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("에러", "IO 예외발생");
            }
            return null;
        }

        /**
         * 이미지 다운로드가 완료되면, 처음에 placeholder 처럼 만들어서 사용하던 Drawable 에, 다운로드 받은 이미지를 넣어준다.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            HTMLTextView HTMLTextView = mWeakRefView.get();

            if (bitmap != null && HTMLTextView != null) {
                BitmapDrawable d = new BitmapDrawable(HTMLTextView.getResources(), bitmap);

                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, HTMLTextView.getWidth(), (bitmap.getHeight() * HTMLTextView.getWidth()) / bitmap.getWidth());
                mDrawable.setLevel(1);

                // 이미지 다운로드 완료 후, invalidate 의 개념으로, 다시한번 텍스트를 설정해준것이다. 더 좋은방법이 있을법도 하다
                CharSequence t = HTMLTextView.getText();
                HTMLTextView.setText(t);
            }
        }


//        public Bitmap resizeBitmapImage(Bitmap source, int maxResolution) {
//            int width = source.getWidth();
//            int height = source.getHeight();
//            int newWidth = width;
//            int newHeight = height;
//            float rate = 0.0f;
//            if (width > height) {
//                if (maxResolution < width) {
//                    rate = maxResolution / (float) width;
//                    newHeight = (int) (height * rate);
//                    newWidth = maxResolution;
//                }
//            } else {
//                if (maxResolution < height) {
//                    rate = maxResolution / (float) height;
//                    newWidth = (int) (width * rate);
//                    newHeight = maxResolution;
//                }
//            }
//            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
//        }
    }
}
