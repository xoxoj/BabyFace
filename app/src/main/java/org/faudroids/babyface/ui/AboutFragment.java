package org.faudroids.babyface.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.faudroids.babyface.R;

import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

public class AboutFragment extends RoboFragment {

    @InjectView(R.id.about_version)
    TextView version_tv;
    @InjectView(R.id.about_own_license)
    TextView own_license_tv;
    @InjectView(R.id.about)
    TextView about_tv;
    @InjectView(R.id.about_content)
    TextView content_tv;
    @InjectView(R.id.about_open_source_licenses)
    TextView about_open_source_licenses_tv;
    @InjectView(R.id.about_used_libraries)
    TextView used_libraries_tv;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String version;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        version_tv.setText(version);
        version_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle(own_license_tv);
            }
        });

        own_license_tv.setText(Html.fromHtml(getString(R.string.about_license)));
        own_license_tv.setVisibility(View.GONE);

        about_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle(content_tv);
            }
        });

        content_tv.setText(Html.fromHtml(getString(R.string.about_content)));
        content_tv.setMovementMethod(LinkMovementMethod.getInstance());
        content_tv.setVisibility(View.GONE);

        about_open_source_licenses_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle(used_libraries_tv);
            }
        });

        used_libraries_tv.setText(Html.fromHtml(getString(R.string.about_used_libraries)));
        used_libraries_tv.setMovementMethod(LinkMovementMethod.getInstance());
        used_libraries_tv.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    private void toogle(TextView tv){
        tv.setVisibility(tv.isShown() ? View.GONE : View.VISIBLE);
    }
}
