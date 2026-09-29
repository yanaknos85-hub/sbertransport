export {};

// import React from 'react';
// import { observer } from 'mobx-react';
// import { Form, Input } from 'antd';
// import { FormInstance } from 'antd/lib/form';
// import classNames from 'classnames';

// import AddressAutoComplete from 'shared/components/AddressAutoComplete/AddressAutoComplete';
// import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
// import { formatAddress } from 'utils/formatAddress';
// import useValidationRules from './useValidationRules';
// import FormText from './FormText';
// import { AddressAutocompleteFieldProps } from '../ModelFormField';

// export default observer((props: AddressAutocompleteFieldProps & { form: FormInstance }) => {
//   const { fieldType, name, placeholder, editable, label, description, onAddressSelect, form, rules, ...other } = props;

//   const fullDataName = `fullData-${name}`;

//   if (!editable) {
//     return <FormText {...props} />;
//   }

//   const handleSelect = (val: WaypointModel) => {
//     if (onAddressSelect) {
//       onAddressSelect(val, name);
//     }
//     form.setFieldsValue({
//       ...form.getFieldsValue(),
//       [name]: formatAddress(val),
//       [fullDataName]: val,
//     });
//   };

//   const validationRules = useValidationRules(props);
//   return (
//     <>
//       {/* This field contains full info about selected address model, but it's hidden from user */}
//       <Form.Item name={fullDataName} noStyle>
//         <Input type="hidden" />
//       </Form.Item>
//       {/* AutoComplete add to field value only formatted address */}
//       <Form.Item
//         name={name}
//         label={label}
//         shouldUpdate
//         rules={validationRules}
//         className={classNames('form-item-address', other.className)}
//       >
//         <AddressAutoComplete onSelect={handleSelect} placeholder={placeholder} value={form.getFieldValue(name)} />
//       </Form.Item>
//     </>
//   );
// });
