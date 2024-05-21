package com.drugbox.domain;

import com.drugbox.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    private boolean isExpDateNotificationEnabled = true;
    private boolean isDrugDisposedNotificationEnabled = true;
    private boolean isDrugAddedNotificationEnabled = true;

    public void setIsExpDateNotificationEnabled(boolean isEnabled) {
        this.isExpDateNotificationEnabled = isEnabled;
    }

    public void setIsDrugDisposedNotificationEnabled(boolean isEnabled) {
        this.isDrugDisposedNotificationEnabled = isEnabled;
    }

    public void setIsDrugAddedNotificationEnabled(boolean isEnabled) {
        this.isDrugAddedNotificationEnabled = isEnabled;
    }
}