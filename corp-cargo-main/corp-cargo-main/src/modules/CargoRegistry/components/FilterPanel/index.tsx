import React, { FC, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { Button, Form } from 'antd';
import cn from 'classnames';

import { FormSectionProps, ModelFormField, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';

import { useFiltersForm } from './hooks/useFiltersForm';
import { FilterPanelProps } from './types';
import styles from './styles.module.scss';

export const FilterPanel: FC<FilterPanelProps> = ({
  onApplyFilters,
  fields,
  setForm,
  className,
  isStatusChangeActive,
  setStatusChangeActive,
  onClearButtonActivator,
  initialValues,
  filterValues,
  resetPagination,
  setIsVisible,
  isVisible,
  onReset,
  onSave,
  withFormSection = false,
  showSaveBtn = false,
  isNewDesign = false,
  submitBtnName,
  disabledSubmit = false,
}) => {
  const { t } = useTranslation();
  const {
    form, onFinish, resetFields,
  } = useFiltersForm(onApplyFilters, onClearButtonActivator);

  const submitForm = (e?: React.FormEvent) => {
    // Предотвращаем стандартное поведение формы, если событие передано
    if (e) {
      e.preventDefault();
    }

    setIsVisible && setIsVisible(false);
    onFinish();
    resetPagination && resetPagination();
    setStatusChangeActive && setStatusChangeActive(false);
  };

  useEffect(() => {
    form.setFieldsValue(filterValues);

    if (setForm) {
      setForm(form);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Вызываем onClearButtonActivator при открытии фильтров для обновления состояния кнопок
  useEffect(() => {
    if (isVisible && onClearButtonActivator) {
      onClearButtonActivator({} as unknown);
    }
  }, [isVisible, onClearButtonActivator]);

  const renderFilterFields = (field: ModelFormFieldProps | FormSectionProps, index: number) => {
    if ('isAvailable' in field && !field.isAvailable) return null;

    if ('filters' in field) {
      return (
        <div
          key={field.title || `section-${index}`}
          className={cn(styles.formSection, {
            [styles.sectionLine]: field.sectionLine,
          })}
        >
          {field.title && <h4 className={styles.sectionName}>{field.title}</h4>}
          <div className={cn(styles.sectionFields, field?.classNames?.sectionFields)}>
            {field.filters.map((f, i) => renderFilterFields(f, i))}
          </div>
        </div>
      );
    }

    return (
      <ModelFormField
        {...field}
        key={field.name || `field-${index}`}
        form={form}
        onChange={value => {
          if (field.onChange) {
            // @ts-ignore
            field.onChange(value);
          }

          // @ts-ignore
          onClearButtonActivator && onClearButtonActivator();
        }}
      />
    );
  };

  const handleReset = () => {
    resetFields();
    resetPagination && resetPagination();
    setStatusChangeActive && setStatusChangeActive(false);
    onReset && onReset();
  };

  return (
    <div
      className={cn(styles.filterPanel, className, {
        [styles.sectionPanel]: withFormSection,
      })}
    >
      <Form
        form={form}
        onFinish={submitForm}
        initialValues={initialValues}
        layout="vertical"
      >
        {fields.map((field, index) => renderFilterFields(field, index))}

        <div className={cn({ formFooter: isNewDesign })}>
          <div className={styles.buttonBar}>
            {showSaveBtn && (
              <Button className={styles.saveFilter} onClick={onSave}>
                {t.global.filterSave}
              </Button>
            )}
            <Button
              onClick={submitForm}
              htmlType="submit"
              type="primary"
              disabled={disabledSubmit}
            >
              {submitBtnName ?? t.FilterPanel.search}
            </Button>
            <Button
              danger
              onClick={handleReset}
              disabled={!isStatusChangeActive}
            >
              {t.FilterPanel.reset}
            </Button>
          </div>
        </div>
      </Form>
    </div>
  );
};
