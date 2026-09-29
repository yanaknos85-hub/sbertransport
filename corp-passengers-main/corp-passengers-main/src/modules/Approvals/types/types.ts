import { FormInstance, Rule } from 'antd/lib/form';
import { LabeledValue } from 'antd/lib/select';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { UUID } from 'utils/io-ts';

export interface ApprovalsRecord {
  transportType?: TransportTypes;

  id?: UUID;
  rowId: UUID;
  minimalSum?: number;

  approvalActive?: boolean;
  approvalDocumentCheck?: boolean;
  affirmativeActive?: boolean;
  tripConfirmationActive?: boolean;
  tripConfirmationDocumentCheck?: boolean;
  tripApprovalActive?: boolean;

  tripPurpose?: {
    rowId: UUID;
    purposeId?: UUID;
  };
  territory: {
    rowId: UUID;
    territory: 'specific' | 'any';
  };

  specificTerritory: {
    rowId: UUID;
    id: UUID | string;
    territory?: string;
  }[];
  specificMinimalSum: {
    rowId: UUID;
    id: UUID;
    minimalSum?: number;
  }[];
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface FormValues extends Record<string, any> {
  type: TransportTypes;
}

export interface PurposeSettings {
  form: FormInstance;
  territory: { territory: string; id: UUID };
  minimalSum: { minimalSum: string; id: UUID };
  specificTerritory: { specificTerritory: string; id: UUID };
  tripPurposes: LabeledValue[];
}

export interface Cell<T> {
  render?: (props: T) => JSX.Element;
  width?: number;
  title?: string;
  fixed?: boolean | 'left' | 'right';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  dataIndex?: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  key?: any;
}

export type CompositeRecord = ApprovalsRecord['tripPurpose'] &
  ApprovalsRecord['territory'] &
  ApprovalsRecord['specificTerritory'] &
  ApprovalsRecord['specificMinimalSum'] & { id: UUID };

export interface ApprovalFieldBase {
  state: ApprovalsRecord[];
  rowId: UUID;
  rules?: Rule[];
  initialValue?: string | number;
}

export interface SpecificTerritoryProps extends ApprovalFieldBase {
  addSpecificTerritory: (id: UUID) => void;
  deleteSpecificTerritory: (rowId: UUID, cellId: UUID) => void | undefined;
  geoZones: LabeledValue[];
  useParentContainer?: boolean;
}
