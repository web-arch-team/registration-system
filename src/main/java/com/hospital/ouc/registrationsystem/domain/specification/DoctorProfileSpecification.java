package com.hospital.ouc.registrationsystem.domain.specification;

import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import com.hospital.ouc.registrationsystem.web.dto.DoctorSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DoctorProfileSpecification {

    public static Specification<DoctorProfile> build(DoctorSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) return cb.and(predicates.toArray(new Predicate[0]));

            if (criteria.getId() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.getId()));
            }

            if (StringUtils.hasText(criteria.getDoctorId())) {
                predicates.add(cb.equal(root.get("doctorId"), criteria.getDoctorId()));
            }

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getGender())) {
                // gender is an enum stored as string (male/female)
                // compare enum as string to avoid type mismatch
                predicates.add(cb.equal(cb.lower(root.get("gender").as(String.class)), criteria.getGender().toLowerCase()));
            }

            if (StringUtils.hasText(criteria.getTitle())) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + criteria.getTitle().toLowerCase() + "%"));
            }

            if (criteria.getDepartmentId() != null) {
                Join<DoctorProfile, Department> dept = root.join("department", JoinType.LEFT);
                predicates.add(cb.equal(dept.get("id"), criteria.getDepartmentId()));
            }

            if (criteria.getDeleted() != null) {
                // in this project, soft-delete is represented by isActive boolean
                // deleted == true -> isActive == false; deleted == false -> isActive == true
                predicates.add(cb.equal(root.get("isActive"), !criteria.getDeleted()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
