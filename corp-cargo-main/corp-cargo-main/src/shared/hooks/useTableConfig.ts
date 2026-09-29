import { RefObject, useEffect, useState } from 'react';

/* в какой-то момент в tableRef возникает ссылка на DOM-элемент, DOM-элемент на странице имеет всякие полезные характеристики, такие как top относительно высоты экрана,
 с помощью getBoundingClientRect(Метод Element.getBoundingClientRect() возвращает размер элемента и его позицию относительно viewport) мы этот top забираем
 и используем, те берем высоту реального viewport, innerHeight та часть окна через которую мы смотрим на сайт, отнимаем все что выше div,
 который мы описали и еще 140 пикселей (пагинация и первая строка в header, потому что эта та часть, которая отличает высоту div и проскролливаемую часть таблицы).
 Те у нас оказывается правильное значение Y, которое мы передаем в скролл.
 */

interface TableScrollConfiguration {
  scrollToFirstRowOnChange: boolean;
  y: number;
  x: number;
}

const MARGIN_TOP = 145;

export const useTableConfig = (
  tableRef: RefObject<HTMLDivElement>,
  tableScrollConfiguration: TableScrollConfiguration
) => {
  const [tableConfig, setTableConfig] = useState(tableScrollConfiguration);

  const handleResize = () => {
    if (!tableRef?.current) {
      return;
    }
    const { top } = tableRef.current.getBoundingClientRect();
    setTableConfig({ ...tableScrollConfiguration, y: window.innerHeight - top - MARGIN_TOP });
  };

  useEffect(() => {
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tableRef?.current]);

  return tableConfig;
};
