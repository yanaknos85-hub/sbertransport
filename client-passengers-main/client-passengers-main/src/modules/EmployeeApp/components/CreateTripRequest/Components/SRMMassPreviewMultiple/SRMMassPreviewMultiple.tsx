/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable arrow-body-style */
import React, {
  FC, useState
} from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import * as ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';

import { StoreNames } from 'stores/StoreNames.enum';

import * as S from './SRMMassPreviewMultiple.style';

interface Props {
  onConfirm: () => void;
  onClose: () => void;
}

const SRMMassPreviewMultiple: FC<Props> = observer(({ /* onConfirm, */ onClose }) => {
  const {
    [StoreNames.srmMassStoreMultiple]: srmMassStoreMultiple, logger,
  } = useAppStoreContext();

  const { requestList = [] } = srmMassStoreMultiple;
  const [isError, setIsError] = useState(false);

  const isDisabled = isError;

  const handleConfirm = async () => {
    logger.toMessage('info', 'file sent');
    onClose();
    // try {
    //   await srmMassStoreMultiple.saveFile(selfEmployee.id);
    //   onConfirm();
    // } catch (err: any) {
    //   logger.toMessage('error', err.message);
    // }
  };

  const handleClose = () => {
    onClose();
  };

  const orderedData = requestList.map(itemRequest => ({
    fields: [
      {
        name: 'Табельный номер', ...itemRequest?.personnelNumber,
      },
      {
        name: 'ФИО', ...itemRequest?.fullName,
      },
      {
        name: 'Адрес посадки', ...itemRequest?.addressFrom,
      },
      {
        name: 'Адрес высадки', ...itemRequest?.addressTo,
      },
      {
        name: 'Время заказа', ...itemRequest?.orderTime,
      },
      {
        name: 'Дата заказа', ...itemRequest?.orderDate,
      },
      {
        name: 'Вид транспорта', ...itemRequest?.transportClass,
      },
      {
        name: 'Тип транспорта', ...itemRequest?.transportType,
      },
      {
        name: 'Цель поездки', ...itemRequest?.tripType,
      },
    ],
  }));

  async function exportJsonToExcel() {
    // Create a new workbook
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Sheet1');

    // Add column headers
    worksheet.columns = [
      { header: 'Табельный номер', key: 'personnelNumber' },
      { header: 'ФИО', key: 'fullName' },
      { header: 'Адрес посадки', key: 'addressFrom' },
      { header: 'Адрес высадки', key: 'addressTo' },
      { header: 'Время заказа', key: 'orderTime' },
      { header: 'Дата заказа', key: 'orderDate' },
      { header: 'Вид транспорта', key: 'transportClass' },
      { header: 'Тип транспорта', key: 'transportType' },
      { header: 'Цель поездки', key: 'tripPurpose' },
    ];

    // Add rows to the worksheet
    requestList.forEach((data, i) => {
      worksheet.addRow({
        personnelNumber: data.personnelNumber.value,
        fullName: data?.fullName?.value,
        addressFrom: data.addressFrom.value,
        addressTo: data.addressTo.value,
        orderTime: data.orderTime.value,
        orderDate: data.orderDate.value,
        transportClass: data.transportClass.value,
        transportType: data.transportType.value,
        tripPurpose: data.tripType.value,
      });

      orderedData[i].fields.forEach((item, j) => {
        const cell = worksheet.lastRow?.getCell(j + 1);

        if (cell && (item?.error || !Object.hasOwn(item, 'error'))) {
          cell.fill = {
            type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFF0000' },
          };
        }
      });
    });

    // Export the workbook to Excel file
    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer],
      { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, 'data.xlsx');
  }

  if (!requestList.length) {
    return (
      <S.ModalStyled
        centered={true}
        closable={true}
        visible={true}
        onCancel={handleClose}
        title={<S.ModalTitleStyled>Ошибка!</S.ModalTitleStyled>}
        footer={<>&nbsp;</>}
      >
        <S.ModalSubTitleStyled>Проверьте корректность загружаемых данных</S.ModalSubTitleStyled>
      </S.ModalStyled>
    );
  }

  return (
    <S.ModalStyled
      width="90%"
      centered={true}
      closable={true}
      visible={true}
      title={(
        <>
          <S.ModalTitleStyled>Превью таблицы Excel</S.ModalTitleStyled>
          {isDisabled ? (
            <S.ModalErrorStyled>
              <S.ModalErrorTitleStyled>В загруженном файле есть ошибки</S.ModalErrorTitleStyled>
              <S.ModalErrorSubTitleStyled>Скачайте файл, внесите изменения и повторите загрузку</S.ModalErrorSubTitleStyled>
            </S.ModalErrorStyled>
          )
            : <S.ModalSubTitleStyled>Проверьте корректность данных перед оформлением заявки</S.ModalSubTitleStyled>}
        </>
      )}
      onCancel={handleClose}
      footer={(
        <S.ButtonsStyled>
          <S.ButtonCancelStyled onClick={handleClose}>Отмена</S.ButtonCancelStyled>
          {isDisabled ? (
            <S.ButtonApproveStyled onClick={exportJsonToExcel}>
              Скачать файл с ошибками
            </S.ButtonApproveStyled>
          )
            : (
              <S.ButtonApproveStyled onClick={handleConfirm}>
                Подтвердить и оформить заявки
              </S.ButtonApproveStyled>
            )}
        </S.ButtonsStyled>
      )}
    >
      <S.ViewportStyled>
        <S.TableStyled>
          <thead>
            <tr>
              <S.ThStyled>№</S.ThStyled>
              {orderedData[0] && orderedData[0].fields
                .map(({ name }) => <S.ThStyled key={name}>{name}</S.ThStyled>)}
            </tr>
          </thead>
          <tbody>
            {orderedData.map((item, i) => (
              <S.TrStyled key={i}>
                <S.TdStyled key={i + 1}>{i + 1}</S.TdStyled>
                {item.fields
                  .map(({
                    name, value, error,
                  }) => {
                    if (error || error === undefined) {
                      if (!isError) setIsError(true);
                      return <S.TdStyledError key={name + i}>{value}</S.TdStyledError>;
                    } else return <S.TdStyled key={name + i}>{value}</S.TdStyled>;
                  })}
              </S.TrStyled>
            )
            )}
          </tbody>
        </S.TableStyled>
      </S.ViewportStyled>
    </S.ModalStyled>
  );
});

export default SRMMassPreviewMultiple;
