package teammates.common.datatransfer.attributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.blobstore.BlobKey;

import teammates.common.util.Assumption;
import teammates.common.util.FieldValidator;
import teammates.common.util.JsonUtils;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StringHelper;
import teammates.storage.entity.StudentProfile;

/**
 * The data transfer object for StudentProfile entities.
 */
public class StudentProfileAttributes extends EntityAttributes<StudentProfile> {

    private static final String STUDENT_PROFILE_BACKUP_LOG_MSG = "Recently modified student profile::";
    private static final String ATTRIBUTE_NAME = "Student Profile";

    // Required
    public String googleId;

    // Optional
    public String shortName;
    public String email;
    public String institute;
    public String nationality;
    public String gender; // only accepts "male", "female" or "other"
    public String moreInfo;
    public String pictureKey;
    public Instant modifiedDate;

    StudentProfileAttributes(String googleId) {
        this.googleId = googleId;
        this.shortName = "";
        this.email = "";
        this.institute = "";
        this.nationality = "";
        this.gender = "other";
        this.moreInfo = "";
        this.pictureKey = "";
        this.modifiedDate = Instant.now();
    }

    public static StudentProfileAttributes valueOf(StudentProfile sp) {
        return builder(sp.getGoogleId())
                .withShortName(sp.getShortName())
                .withEmail(sp.getEmail())
                .withInstitute(sp.getInstitute())
                .withGender(sp.getGender())
                .withNationality(sp.getNationality())
                .withMoreInfo(sp.getMoreInfo())
                .withPictureKey(sp.getPictureKey().getKeyString())
                .withModifiedDate(sp.getModifiedDate())
                .build();
    }

    /**
     * Return new builder instance all string fields setted to {@code ""}
     * and with {@code gender = "other"}.
     */
    public static Builder builder(String googleId) {
        return new Builder(googleId);
    }

    public StudentProfileAttributes getCopy() {
        return builder(googleId)
                .withShortName(shortName)
                .withEmail(email)
                .withInstitute(institute)
                .withGender(gender)
                .withNationality(nationality)
                .withMoreInfo(moreInfo)
                .withPictureKey(pictureKey)
                .withModifiedDate(modifiedDate)
                .build();
    }

    @Override
    public List<String> getInvalidityInfo() {
        FieldValidator validator = new FieldValidator();
        List<String> errors = new ArrayList<>();

        addNonEmptyError(validator.getInvalidityInfoForGoogleId(googleId), errors);

        // accept empty string values as it means the user has not specified anything yet.

        if (!StringHelper.isEmpty(shortName)) {
            addNonEmptyError(validator.getInvalidityInfoForPersonName(shortName), errors);
        }

        if (!StringHelper.isEmpty(email)) {
            addNonEmptyError(validator.getInvalidityInfoForEmail(email), errors);
        }

        if (!StringHelper.isEmpty(institute)) {
            addNonEmptyError(validator.getInvalidityInfoForInstituteName(institute), errors);
        }

        if (!StringHelper.isEmpty(nationality)) {
            addNonEmptyError(validator.getInvalidityInfoForNationality(nationality), errors);
        }

        addNonEmptyError(validator.getInvalidityInfoForGender(gender), errors);

        Assumption.assertNotNull(this.pictureKey);

        // No validation for modified date as it is determined by the system.
        // No validation for More Info. It will properly sanitized.

        return errors;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, StudentProfileAttributes.class);
    }

    @Override
    public StudentProfile toEntity() {
        return new StudentProfile(googleId, shortName, email, institute, nationality, gender,
                                  moreInfo, new BlobKey(this.pictureKey));
    }

    @Override
    public String getIdentificationString() {
        return this.googleId;
    }

    @Override
    public String getEntityTypeAsString() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public String getBackupIdentifier() {
        return STUDENT_PROFILE_BACKUP_LOG_MSG + googleId;
    }

    @Override
    public String getJsonString() {
        return JsonUtils.toJson(this, StudentProfileAttributes.class);
    }

    @Override
    public void sanitizeForSaving() {
        this.googleId = SanitizationHelper.sanitizeGoogleId(this.googleId);
    }

    /**
     * A Builder class for {@link StudentProfileAttributes}.
     */
    public static class Builder {
        private static final String REQUIRED_FIELD_CANNOT_BE_NULL = "Required field cannot be null";

        private final StudentProfileAttributes profileAttributes;

        public Builder(String googleId) {
            Assumption.assertNotNull(REQUIRED_FIELD_CANNOT_BE_NULL, googleId);
            profileAttributes = new StudentProfileAttributes(googleId);
        }

        public Builder withShortName(String shortName) {
            if (shortName != null) {
                profileAttributes.shortName = SanitizationHelper.sanitizeName(shortName);
            }
            return this;
        }

        public Builder withEmail(String email) {
            if (email != null) {
                profileAttributes.email = SanitizationHelper.sanitizeEmail(email);
            }
            return this;
        }

        public Builder withInstitute(String institute) {
            if (institute != null) {
                profileAttributes.institute = SanitizationHelper.sanitizeTitle(institute);
            }
            return this;
        }

        public Builder withNationality(String nationality) {
            if (nationality != null) {
                profileAttributes.nationality = SanitizationHelper.sanitizeName(nationality);
            }
            return this;
        }

        public Builder withGender(String gender) {
            profileAttributes.gender = isGenderValid(gender) ? gender : "other";
            return this;
        }

        public Builder withMoreInfo(String moreInfo) {
            if (moreInfo != null) {
                profileAttributes.moreInfo = moreInfo;
            }
            return this;
        }

        public Builder withPictureKey(String pictureKey) {
            if (pictureKey != null) {
                profileAttributes.pictureKey = pictureKey;
            }
            return this;
        }

        public Builder withModifiedDate(Instant modifiedDate) {
            profileAttributes.modifiedDate = modifiedDate == null ? Instant.now() : modifiedDate;
            return this;
        }

        public StudentProfileAttributes build() {
            return profileAttributes;
        }

        private boolean isGenderValid(String gender) {
            return "male".equals(gender) || "female".equals(gender) || "other".equals(gender);
        }
    }
}
