package net.macdidi.convencard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    // 畫面資源編號
    private int resource;
    // 包裝的記事資料
    private List<Item> items;
    private String fileName;

    public ItemAdapter(Context context, int resource, List<Item> items) {
        super(context, resource, items);
        this.resource = resource;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        // 讀取目前位置的記事物件
        final Item item = getItem(position);

        if (convertView == null) {
            // 建立項目畫面元件
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater)
                    getContext().getSystemService(inflater);
            li.inflate(resource, itemView, true);
        }
        else {
            itemView = (LinearLayout) convertView;
        }

        // 讀取記事顏色、已選擇、標題與日期時間元件
        RelativeLayout typeColor = (RelativeLayout) itemView.findViewById(R.id.type_color);
        ImageView selectedItem = (ImageView) itemView.findViewById(R.id.selected_item);
        TextView titleView = (TextView) itemView.findViewById(R.id.title_text);
        TextView dateView = (TextView) itemView.findViewById(R.id.date_text);
        ImageView picture =(ImageView) itemView.findViewById(R.id.picture_item);
        // 設定記事顏色
        GradientDrawable background = (GradientDrawable)typeColor.getBackground();
        background.setColor(item.getColor().parseColor());


        // 設定標題與日期時間
        titleView.setText(item.getTitle());
        dateView.setText(item.getLocaleDatetime());

        if (item.getFileName() != null && item.getFileName().length() > 0) {
            // 照片檔案物件
            File file = configFileName("P", ".jpg",item);

            // 如果照片檔案存在
            if (file.exists()) {
                // 顯示照片元件
                picture.setVisibility(View.VISIBLE);
                // 設定照片
                FileUtil.fileToImageView(file.getAbsolutePath(), picture);
            }
        }
        // 設定是否已選擇
        selectedItem.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);
        fileName=null;
        return itemView;
    }

    // 設定指定編號的記事資料
    public void set(int index, Item item) {
        if (index >= 0 && index < items.size()) {
            items.set(index, item);
            notifyDataSetChanged();
        }
    }

    // 讀取指定編號的記事資料
    public Item get(int index) {
        return items.get(index);
    }
    private File configFileName(String prefix, String extension,Item item) {
        // 如果記事資料已經有檔案名稱
        if (item.getFileName() != null && item.getFileName().length() > 0) {
            fileName = item.getFileName();
        }
        // 產生檔案名稱
        else {
            fileName = FileUtil.getUniqueFileName();
        }

        return new File(FileUtil.getExternalStorageDir(FileUtil.APP_DIR),
                prefix + fileName + extension);
    }
}

