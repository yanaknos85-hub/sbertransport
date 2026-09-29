import React from 'react';
import type { FC } from 'react';
import { Form, TreeSelect as Select } from 'antd';
import { TreeSelectFieldProps } from '../ModelFormField';

const TreeSelect: FC<TreeSelectFieldProps> = props => {
  const {
    label, name, disabled, placeholder, className, options, onChange, onClear,
  } = props;

  return (
    <Form.Item name={name} label={label}>
      <Select
        allowClear
        showArrow
        treeCheckable
        className={className}
        placeholder={placeholder}
        disabled={disabled}
        // @ts-ignore
        treeData={options}
        // @ts-ignore
        onChange={onChange}
        onClear={onClear}
        showCheckedStrategy={Select.SHOW_ALL}
        filterTreeNode={(value, treeNode) => value.length > 2 && typeof treeNode?.title === 'string'
          ? treeNode.title.toLowerCase().includes(value.toLowerCase())
          : true}
      />
    </Form.Item>
  );
};

export default TreeSelect;
