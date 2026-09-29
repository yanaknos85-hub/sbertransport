import React from 'react';
import { Form, Select } from 'antd';
import { FormInstance } from 'antd/lib/form';
import NewSelect from 'shared/form/Select/Select';
import cn from 'classnames';
import { SelectFieldProps } from '../ModelFormField';
import useValidationRules from './useValidationRules';
import styles from '../modelDetail.module.scss';

export default (
  props: SelectFieldProps & { initialValue?: string | number; isNewDesign?: boolean; useParentContainer?: boolean }
): JSX.Element => {
  const {
    fieldType,
    name,
    editable,
    label,
    description,
    disabled,
    rules,
    initialValue,
    onChange,
    isNewDesign,
    useParentContainer = true,
    ...other
  } = props;
  const options = typeof props.options === 'function' ? props.options() : props.options;

  if (!editable) {
    // TODO like a FormText?
    const renderText = (form: FormInstance) => {
      const getLabel = (value: string) => options.find(opt => opt.value === value)?.label ?? value;

      const fieldValue = form.getFieldValue(name);
      if (typeof fieldValue === 'string') {
        return getLabel(fieldValue);
      }

      return (fieldValue || []).map(getLabel).join(', ');
    };
    return (
      <Form.Item
        label={label}
        shouldUpdate
        className={cn({ [styles.formItem]: isNewDesign })}
        colon={!isNewDesign}
      >
        {renderText}
      </Form.Item>
    );
  }
  const validationRules = useValidationRules(props);

  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
      rules={validationRules}
      initialValue={initialValue}
      className={cn({ [styles.formItem]: isNewDesign })}
      colon={!isNewDesign}
    >
      {isNewDesign ? (
        <NewSelect
          showSearch
          optionFilterProp="label"
          onChange={onChange}
          {...other}
          options={options}
          disabled={disabled}
          getPopupContainer={useParentContainer ? trigger => trigger.parentNode : undefined}
        />
      ) : (
        <Select
          showSearch
          optionFilterProp="label"
          onChange={onChange}
          {...other}
          options={options}
          disabled={disabled}
          getPopupContainer={useParentContainer ? trigger => trigger.parentNode : undefined}
        />
      )}
    </Form.Item>
  );
};
