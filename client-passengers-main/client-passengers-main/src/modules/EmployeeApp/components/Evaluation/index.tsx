/* eslint-disable no-unused-expressions */
import { UUID } from 'utils/io-ts';
import { observer } from 'mobx-react';
import React, { useEffect, useState } from 'react';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';
import { TModal } from 'shared/ui/Modal/Modal';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequest } from 'stores/Trip/Trip.interface';
import Process from './Constants/Process';
import PersonalCreator from './Creators/PersonalCreator';
import PublicCreator from './Creators/PublicCreator';
import TaxiCreator from './Creators/TaxiCreator';
import { IProduct } from './types';
import View from './View';

/**
 * 1 из 3
 *
 * Для работы компонента "Оценка" используется паттерн Фабричный Метод.
 * https://github.com/RefactoringGuru/design-patterns-typescript/blob/main/src/FactoryMethod/Conceptual/index.ts.
 * Излюбленный метод для набора сущностей с большим куском общего кода.
 *
 * В конце концов для создания новой модалки (сущности) оценки нужно сделать 3 простые вещи:
 * 1. Создать конкретный класс создателя отнаследовав себя от абстрактного.
 * 2. Создать конкретный класс продукта, переопредилив отличающиеся абстрактные методы (работа с апи, работа с текстами и иконками).
 * Кстати VSCode помогает в работе с абстрактными классами.
 * 3. Вызвать этот класс в зависимости от вида модалки.
 *
 * Все :)
 *
 * P.S. Если хотите представить, как может быть "по-другому" предлагаю посмотреть код реестров. Беру на себя отвественность по унижению чужого кода
 * в связи с тем, что сам его и писал :)
 * */

const Evaluation: React.FC<{ transportType: TransportTypeEnum; request: TripRequest; isRated: boolean }> = observer(
  ({
    transportType, request, isRated = false,
  }) => {
    const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

    const [product, setProduct] = useState<IProduct>();

    const {
      advantages,
      drawbacks,
      rating,
      ratingComment,
      setAdvantages,
      setDrawbacks,
      setRating,
      setRatingComment,
      clearAdvantages,
      clearDrawbacks,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      onOpen = () => {},
      setIsPositive,
    } = product || {};

    const methodProps = {
      setAdvantages,
      setDrawbacks,
      setRating,
      setRatingComment,
      clearAdvantages,
      clearDrawbacks,
      setIsPositive,
    };

    const isReadOnly = product?.process === Process.START && isRated;

    const handleConfirm = () => {
      let result = Promise.resolve(200);

      if (product) {
        product.rating && product.rating >= 4
          ? (result = product.onSuccess({
            rating, advantages, drawbacks, ratingComment,
          }, request?.id as UUID))
          : (result = product.onFailure({
            rating, advantages, drawbacks, ratingComment,
          }, request?.id as UUID));
      }

      return result;
    };

    useEffect(() => {
      switch (transportType) {
        case TransportTypeEnum.TAXI:
          setProduct(new TaxiCreator().factoryMethod(tripStore, request));
          break;
        case TransportTypeEnum.PUBLIC:
          setProduct(new PublicCreator().factoryMethod(tripStore, request));
          break;
        case TransportTypeEnum.PERSONAL:
          setProduct(new PersonalCreator().factoryMethod(tripStore, request));
          break;
        default:
          setProduct(new TaxiCreator().factoryMethod(tripStore, request));
      }
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <>
        {product && (
          <TModal
            properties={{
              title: isReadOnly ? 'Ваша оценка' : 'Поездка завершена',
              handleConfirm,
              confirmButton: { text: 'Оценить' },
              declineButton: { text: 'Отменить', hide: true },
            }}
            isPoppup={product.process !== Process.START && product.process !== Process.CLOSE}
            isReadOnly={isReadOnly}
            process={product.process}
            onClose={product.onClose}
          >
            <View
              isRated={isRated}
              isReadOnly={isReadOnly}
              request={request}
              {...product}
              {...methodProps}
            />
          </TModal>
        )}

        <TButton
          onClick={e => {
            onOpen();
            e.stopPropagation();
          }}
          $size="small"
        >
          {isRated ? 'Ваша оценка' : 'Оценить'}
        </TButton>
      </>
    );
  }
);

export default Evaluation;
