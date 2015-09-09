package org.faudroids.babyface.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.photo.ReminderPeriod;
import org.faudroids.babyface.photo.ReminderUnit;

/**
 * Inits the reminder period layout and handles click events on it.
 */
public class ReminderPeriodViewHandler {

	private final Context context;
	private final ViewGroup[] regularRowView = new ViewGroup[5];
	private final ViewGroup[] customRowViews = new ViewGroup[4];
	private final View regularView, customView;
	private final EditText amountEditText;

	private int selectedIdx;

	public ReminderPeriodViewHandler(View view) {
		this.context = view.getContext();
		regularView = view.findViewById(R.id.layout_regular);
		regularRowView[0] = (ViewGroup) view.findViewById(R.id.row_1);
		regularRowView[1] = (ViewGroup) view.findViewById(R.id.row_2);
		regularRowView[2] = (ViewGroup) view.findViewById(R.id.row_3);
		regularRowView[3] = (ViewGroup) view.findViewById(R.id.row_4);
		regularRowView[4] = (ViewGroup) view.findViewById(R.id.row_5);

		customView = view.findViewById(R.id.layout_custom);
		amountEditText = (EditText) view.findViewById(R.id.edit_amount);
		customRowViews[0] = (ViewGroup) view.findViewById(R.id.row_hours);
		customRowViews[1] = (ViewGroup) view.findViewById(R.id.row_days);
		customRowViews[2] = (ViewGroup) view.findViewById(R.id.row_weeks);
		customRowViews[3] = (ViewGroup) view.findViewById(R.id.row_months);

		// setup on clicks
		for (int idx = 0; idx < regularRowView.length; ++idx) {
			final int clickedIdx = idx;
			regularRowView[idx].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// switch to custom view
					if (clickedIdx == 4) {
						customView.setVisibility(View.VISIBLE);
						regularView.setVisibility(View.GONE);
						toggleSelected(customRowViews, 1);
						amountEditText.requestFocus();
						amountEditText.selectAll();
						return;
					}
					toggleSelected(regularRowView, clickedIdx);
				}
			});
		}
		for (int idx = 0; idx < customRowViews.length; ++idx) {
			final int clickedIdx = idx;
			customRowViews[idx].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					toggleSelected(customRowViews, clickedIdx);
				}
			});
		}
	}


	public ReminderPeriod getReminderPeriod() {
		long reminderUnit = Long.MAX_VALUE;
		int amount = 1;

		if (customView.getVisibility() != View.VISIBLE) {
			switch (selectedIdx) {
				case 0: // never
					reminderUnit = ReminderUnit.MONTH;
					amount = 0;
					break;
				case 1: // one day
					reminderUnit = ReminderUnit.DAY;
					break;
				case 2: // one week
					reminderUnit = ReminderUnit.WEEK;
					break;
				case 3: // one month
					reminderUnit = ReminderUnit.MONTH;
					break;
			}
		} else {
			amount = Integer.valueOf(amountEditText.getText().toString());
			switch (selectedIdx) {
				case 0: // hours
					reminderUnit = ReminderUnit.HOUR;
					break;
				case 1: // days
					reminderUnit = ReminderUnit.DAY;
					break;
				case 2: // weeks
					reminderUnit = ReminderUnit.WEEK;
					break;
				case 3: // months
					reminderUnit = ReminderUnit.MONTH;
					break;
			}
		}
		return new ReminderPeriod(reminderUnit, amount);
	}


	public void setReminderPeriod(ReminderPeriod period) {
		boolean isCustom = (period.getUnitInSeconds() == ReminderUnit.HOUR) || (period.getAmount() != 1 && period.getAmount() != 0);

		// show / hide appropriate container view
		customView.setVisibility(isCustom ? View.VISIBLE : View.GONE);
		regularView.setVisibility(isCustom ? View.GONE : View.VISIBLE);

		// set selected row
		int rowIdx = 0;
		if (period.getUnitInSeconds() == ReminderUnit.DAY) rowIdx = 1;
		else if (period.getUnitInSeconds() == ReminderUnit.WEEK) rowIdx = 2;
		else if (period.getUnitInSeconds() == ReminderUnit.MONTH) rowIdx = 3;
		if (period.getAmount() == 0) rowIdx = 0;
		if (isCustom) toggleSelected(customRowViews, rowIdx);
		else toggleSelected(regularRowView, rowIdx);


		// set amount
		if (isCustom) amountEditText.setText(String.valueOf(period.getAmount()));
	}


	private void toggleSelected(ViewGroup[] rowViews, int selectedIdx) {
		this.selectedIdx = selectedIdx;
		for (int idx = 0; idx < rowViews.length; ++idx) {
			int txtColor = (idx == selectedIdx) ? context.getResources().getColor(R.color.accent) : context.getResources().getColor(android.R.color.white);
			int imgVisibility = (idx == selectedIdx) ? View.VISIBLE : View.GONE;
			((TextView) rowViews[idx].getChildAt(0)).setTextColor(txtColor);
			rowViews[idx].getChildAt(1).setVisibility(imgVisibility);
		}
	}
}
