import React, { FC, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { Button, Form } from 'antd';
import cn from 'classnames';

import { FormSectionProps, ModelFormField, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';

import { useFiltersForm } from './hooks/useFiltersForm';
import { FilterPanelProps } from './types';
import { Icon } from 'shared/components/Icon';
import styles from './styles.module.scss';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

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
  onSave,
  withFormSection = false,
  isNewDesign = false,
  submitBtnName,
}) => {
  const { t } = useTranslation();
  const {
    form, onFinish, resetFields,
  } = useFiltersForm(onApplyFilters);
  const { logger } = useAppStoreContext();

  const submitForm = () => {
    form.validateFields().then(
      () => {
        setIsVisible && setIsVisible(false);
        onFinish();
        resetPagination && resetPagination();
      },
      () => {
        logger.toMessage('error', 'Проверьте заполнение полей формы!');
      }
    );
  };

  useEffect(() => {
    form.setFieldsValue(filterValues);

    if (setForm) {
      setForm(form);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const renderFilterFields = (field: ModelFormFieldProps | FormSectionProps) => {
    if ('isAvailable' in field && !field.isAvailable) return null;

    if ('filters' in field) {
      return (
        <div
          className={cn(styles.formSection, {
            [styles.sectionLine]: field.sectionLine,
          })}
        >
          {field.title && <h4 className={styles.sectionName}>{field.title}</h4>}
          <div className={cn(styles.sectionFields, field?.classNames?.sectionFields)}>
            {field.filters.map(renderFilterFields)}
          </div>
        </div>
      );
    }

    return (
      <ModelFormField
        {...field}
        key={field.name}
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

  const handleSave = () => {
    form.validateFields().then(
      () => {
        onSave && onSave();
        onFinish();
        setIsVisible && setIsVisible(false);
      },
      () => {
        logger.toMessage('error', 'Проверьте заполнение полей формы!');
      }
    );
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
        {fields.map(renderFilterFields)}

        <div className={cn({ formFooter: isNewDesign })}>
          <div className={styles.buttonBar}>

            <Button
              className={styles.saveFilter}
              onClick={handleSave}
            >
              <Icon type="import" />
              {t.global.uploadReport}
            </Button>

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
