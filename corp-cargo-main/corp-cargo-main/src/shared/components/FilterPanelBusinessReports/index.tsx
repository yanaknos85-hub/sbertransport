import React, { FC, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { Button, Form } from 'antd';
import cn from 'classnames';
import { FormSectionProps, ModelFormField, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { useFiltersForm } from '../FilterPanel/hooks/useFiltersForm';
import { FilterPanelProps } from './types';
import { processCreateSubmitJson } from 'modules/CargoBusinessReports/utils/utils';
import { useCreateTask } from 'api/business-reports';
import { useBusinessReportsContext } from 'modules/CargoBusinessReports/context/BusinessReports.context';

import styles from './styles.module.scss';

export const FilterPanelBusinessReports: FC<FilterPanelProps> = ({
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
  onReset,
  withFormSection = false,
  isNewDesign = false,
  submitBtnName,
}) => {
  const { t } = useTranslation();
  const {
    form, resetFields,
  } = useFiltersForm(onApplyFilters);

  const [createTask] = useCreateTask();

  const { initialFilters } = useBusinessReportsContext();
  const submitForm = async e => {
    e.preventDefault();
    await createTask(processCreateSubmitJson(form.getFieldsValue()));
    setIsVisible && setIsVisible(false);
    form.resetFields();
    setStatusChangeActive && setStatusChangeActive(false);
  };

  useEffect(() => {
    form.setFieldsValue(filterValues);

    if (setForm) {
      setForm(form);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!initialFilters) return;

    form.setFieldsValue(initialFilters);

    if (setForm) {
      setForm(form);
    }
  }, [initialFilters]);

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
          onClearButtonActivator();
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
        initialValues={initialValues}
        layout="vertical"
      >
        {fields.map((field, index) => renderFilterFields(field, index))}

        <div className={cn({ formFooter: isNewDesign })}>
          <div className={styles.buttonBar}>
            <Button
              onClick={submitForm}
              htmlType="submit"
              type="primary"
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
