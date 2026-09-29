import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import { Position } from 'stores/Position/Position.interface';
import { TaxiClass, TaxiClassDescriptions } from 'stores/Trip/Trip.interface';
import { useOrganizations } from 'api/organizations';

export type PositionRecord = Pick<
  Position,
  'positionName' | 'selfApproved' | 'availableClasses' | 'id' | 'active' | 'humanReadableId'
>;

export const useColumns: () => ColumnProps<PositionRecord>[] = () => {
  const { data: organizations } = useOrganizations();
  const { t } = useTranslation();
  return [
    {
      title: t.Positions.Active,
      dataIndex: 'active',
      key: 'active',
      width: 200,
      sorter: (a: PositionRecord, b: PositionRecord) => (a.active ? (b.active ? 0 : 1) : -1),
      render: (value?: boolean) => t.Positions[value ? 'Active.True' : 'Active.False'],
      defaultSortOrder: 'descend',
    },
    {
      title: t.Positions.PositionId,
      dataIndex: 'humanReadableId',
      key: 'humanReadableId',
      width: 150,
      sorter: (a: PositionRecord, b: PositionRecord) => (a.humanReadableId > b.humanReadableId ? -1 : 1),
      defaultSortOrder: 'descend',
    },
    {
      title: t.Positions.PositionName,
      dataIndex: 'positionName',
      key: 'positionName',
      sorter: (a: PositionRecord, b: PositionRecord) => a.positionName.localeCompare(b.positionName),
    },
    {
      title: t.Positions.Organization,
      dataIndex: 'organizationId',
      key: 'organization',
      render: id => organizations.byId[id]?.officialName || id,
    },
    {
      title: t.Positions.SelfApproved,
      dataIndex: 'selfApproved',
      key: 'selfApproved',
      width: 250,
      render: (value?: boolean) => t.Positions[value ? 'SelfApproved.True' : 'SelfApproved.False'],
      sorter: (a: PositionRecord, b: PositionRecord) => (a.selfApproved ? (b.selfApproved ? 0 : 1) : -1),
    },
    {
      title: t.Positions.AvailableClasses,
      dataIndex: 'availableClasses',
      key: 'availableClasses',
      render: (availableClasses: TaxiClass[]) => availableClasses.map((availableClass: TaxiClass) => TaxiClassDescriptions[availableClass]).join(', '),
      // eslint-disable-next-line @stylistic/max-len
      sorter: (a: PositionRecord, b: PositionRecord) => (a.availableClasses || []).length > (b.availableClasses || []).length ? 1 : -1,
    },
  ];
};
