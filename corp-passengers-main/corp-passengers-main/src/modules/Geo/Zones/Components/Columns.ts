import { useTranslation } from 'i18n';
import { ColumnProps } from 'antd/lib/table';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';

export type GeoZoneRecord = Pick<GeoZones, 'id' | 'name' | 'code'>;

export const useColumns: () => ColumnProps<GeoZoneRecord>[] = () => {
  const { t } = useTranslation();
  return [
    {
      title: t.Forms.Geo.id,
      dataIndex: 'id',
      key: 'id',
      width: 280,
    },
    {
      title: t.Forms.Geo.name,
      dataIndex: 'name',
      key: 'name',
      width: 280,
    },
    {
      title: t.Forms.Geo.code,
      dataIndex: 'code',
      key: 'code',
    },
  ];
};
