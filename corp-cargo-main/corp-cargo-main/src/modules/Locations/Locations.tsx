import React from 'react';
import { observer } from 'mobx-react';
import { useRouteMatch } from 'react-router-dom';
import { ButtonProps } from 'antd/lib/button';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { Location } from 'stores/Locations/Locations.interface';
import { useDeleteLocation, useLocations, useUploadLocations } from 'api/locations';
import { formatAddress } from 'utils/formatAddress';
import DownloadButton from 'shared/components/DownloadButton/DownloadButton';
import { Pagination } from 'shared/components/PaginationWithPageSelect/Pagination';
import { TableShadow } from 'shared/components/TableShadow';
import { Button } from 'shared/components/Button/Button';
import { Icon } from 'shared/components/Icon';
import { useQuery } from 'shared/hooks/useQuery';
import { useModalState } from 'shared/hooks/useModal';
import { ignore } from 'utils';
import cn from 'classnames';
import { Modal } from 'shared/components/Modal/Modal';
import { ToolbarProvider } from 'components/Toolbar';
import { PanelTitle } from 'components/Panel';
import { LocationDetailed } from './Components/LocationDetailed';
import { UploadButton, importExportEndpointMap } from '../UploadButton';
import { LocationsTextsCyrillic, LocationsTexts } from './Locations.constants';
import { columns } from './Components/Columns';
import styles from './Locations.module.scss';

const UploadIconButton: React.FC = (props: ButtonProps) => (
  <Button className={styles.iconButton} {...props}>
    <Icon type="export" />
  </Button>
);

export const Locations: React.FC<{ className?: string }> = observer(({ className }) => {
  const [deleteLocation] = useDeleteLocation();
  const [locationId, setLocationId] = React.useState<string>('adding');

  const { locations } = useLocations().data;
  const { query, setPagination } = useQuery();
  const [visible, { hide, show }] = useModalState();
  const preparedLocation = locations.map(location => ({
    ...location,
    fullAddress: formatAddress(location.address),
  }));

  const match = useRouteMatch();

  const handleClick = (): void => {
    show();
  };

  const handleEdit = (id: string): void => {
    setLocationId(id);
    show();
  };

  const handleClose = (): void => {
    setLocationId('');
    hide();
  };

  const handleAddNewClick = (): void => {
    setLocationId('adding');
    show();
  };

  const deleteButtons = {
    fixed: 'right',
    width: 60,
    render: (_: any, record: any): JSX.Element => (
      <TableEditButtons
        onEdit={handleEdit}
        path={match.path}
        id={record.id}
        onDelete={() => {
          deleteLocation({ locId: record.id }).then(ignore);
        }}
        cancelText={LocationsTextsCyrillic[LocationsTexts.cancel]}
        okText={LocationsTextsCyrillic[LocationsTexts.remove]}
        title={LocationsTextsCyrillic[LocationsTexts.deleteConfirm]}
      />
    ),
  };

  const columnsProps = columnsPropsFactory<Location>([...columns, deleteButtons as any]);
  const currentRows = preparedLocation.slice(query.page * query.size, query.page * query.size + query.size);

  return (
    <div className={styles.tableWrapper}>
      <div className={styles.buttonGroup}>
        <UploadButton
          entity="meetingAddress"
          useUpload={useUploadLocations}
          customElement={<UploadIconButton />}
        />
        <DownloadButton url={`${importExportEndpointMap.location}/files/meetingAddress`} iconColor="#4D4D4D" />
      </div>
      <div className={cn(className)} style={{ width: 'inherit', padding: '0 20px' }}>
        <TableShadow
          columns={columnsProps}
          dataSource={currentRows}
          pagination={false}
          bordered
          style={{ overflow: 'scroll' }}
          scroll={{ x: 700 }}
          rowKey="id"
          size="small"
          tableLayout="auto"
          footer={() => <LocationsFooter handleAddNewClick={handleAddNewClick} />}
          className="expanded_filters_table"
        />
        <Modal
          onCancel={handleClose}
          visible={visible}
          footer={null}
        >
          <LocationDetailed
            locationId={locationId}
            handleClose={handleClose}
            key={locationId}
          />
        </Modal>
      </div>
      <Pagination
        pagination={query}
        setPagination={setPagination}
        total={locations.length}
      />
    </div>
  );
});

const LocationsFooter = ({ handleAddNewClick }: { handleAddNewClick: () => void }) => (
  <div className="table_footer">
    <Button size="small" onClick={handleAddNewClick}>
      {LocationsTextsCyrillic[LocationsTexts.addNew]}
    </Button>
  </div>
);

const withProvider = observer(() => (
  <ToolbarProvider>
    <PanelTitle>Справочник «Корпоративные адреса»</PanelTitle>
    <Locations />
  </ToolbarProvider>
));

export default withProvider;
