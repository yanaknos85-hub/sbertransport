import { LabeledValue } from "antd/lib/select";

export type AccessControl = 'PERSONAL' | 'ORGANIZATION' | 'PUBLIC';

export type AccessLabeledValue = LabeledValue & {
  value: AccessControl;
}