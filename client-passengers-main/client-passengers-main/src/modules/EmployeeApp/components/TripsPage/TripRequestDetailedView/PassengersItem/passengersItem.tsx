/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import React, { useEffect, useState } from 'react';
import styles from '../styles.module.scss';
import User from 'shared/components/Images/user.png';
import Comment from 'shared/components/Images/comment.svg';
import { IRequestsSharedModal } from 'stores/Trip/Trip.interface';
import { formatFullName } from 'utils/formatFullName';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { parseNumber } from 'utils/parseNumber';
import classNames from 'classnames';
import { TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';

interface PassengersItemProps {
  item: IRequestsSharedModal;
}

const PassengersItem: React.FC<PassengersItemProps> = ({
  item,
}) => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  const [avatar, setAvatar] = useState<string>('');
  const [showFullText, setShowFullText] = useState(false);

  useEffect(() => {
    tripStore.getUserAvatart(item.userId ? item.userId : item.employee?.userId)
      .then(setAvatar);
  }, []);

  const handleReadMore = () => {
    setShowFullText(true);
  };

  const firstName = item.firstName ? item.firstName : item?.employee?.firstName;
  const lastName = item.lastName ? item.lastName : item?.employee?.lastName;
  const patronymic = item.patronymic ? item.patronymic : item?.employee?.patronymic;
  const mobilePhone = item.mobilePhone ? item.mobilePhone : item.employee?.mobilePhone;

  const canceledRequests = item?.status === TripStatusesEnum.PERSONAL_CANCELLED || item?.status === TripStatusesEnum.TAXI_CANCELLED;

  return (
    <div className={styles.informationPassengersWrapper}>
      <div className={styles.informationPassengersMain}>
        <img alt="avatar" src={avatar ? avatar : User} />
        <div className={styles.informationPassengersInfo}>
          <span className={classNames(canceledRequests ? styles.informationCancelledPassengersInfoName : styles.informationPassengersInfoName)}>
            {formatFullName(firstName, lastName, patronymic)}
          </span>
          {canceledRequests
            ? <span className={styles.informationCancelledPassengers}>{item?.statusDescription}</span>
            : (
              <>
                <span className={styles.informationPassengersInfoPosition}>
                  {item.employee?.positionName}
                </span>
                <span className={styles.informationPassengersInfoNumber}>
                  {parseNumber(mobilePhone)}
                </span>
              </>
            )}
        </div>
      </div>
      {item.commentForDriver && !canceledRequests
      && (
      <>
        <div className={styles.informationPassengersCommentWrapper}>
          <div className={styles.informationPassengersComment}>
            <img alt="comment" src={Comment} />
          </div>
          <div className={styles.informationPassengersCommentTextWrapper}>
            <span className={styles.informationPassengersCommentText}>
              {showFullText ? item.commentForDriver : item.commentForDriver.slice(0, 60)}
            </span>
            {!showFullText && item.commentForDriver.length > 60
            && (
            <p
              className={styles.informationPassengersCommentButtonMore}
              onClick={handleReadMore}
            >
              читать далее
            </p>
            )}
          </div>
        </div>
      </>
      )}
    </div>
  );
};

export default PassengersItem;
