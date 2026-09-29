import { Rule } from 'antd/lib/form';

import { ValidationRules } from 'shared/fieldValidationRules';

import { ModelFormFieldProps } from '../ModelFormField';

const checkRequiredCustomRule = (rules: Rule[]): boolean => rules.some(rule => 'required' in rule);

const isRequireInRules = (isRequired: boolean | undefined, rules: Rule[]): Rule[] => (
  isRequired ? [ValidationRules.general.required, ...rules] : rules
);

export default (props: Partial<ModelFormFieldProps>): Rule[] => {
  const validationRules: Rule[] = props.rules || [];
  const isCustomRequire = checkRequiredCustomRule(validationRules);
  return isCustomRequire ? validationRules : isRequireInRules(props.required, validationRules);
};
