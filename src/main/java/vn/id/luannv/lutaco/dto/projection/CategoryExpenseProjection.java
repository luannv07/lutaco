package vn.id.luannv.lutaco.dto.projection;

public interface CategoryExpenseProjection {
    String getCategoryParentName();

    Long getTotal();

    Double getPct();
}
