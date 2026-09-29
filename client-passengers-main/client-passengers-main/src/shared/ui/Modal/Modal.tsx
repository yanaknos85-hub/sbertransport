import React, {
  Dispatch,
  ReactPortal,
  SetStateAction as SSA,
  MouseEvent as RMouseEvent,
  useState,
  useEffect
} from 'react';
import ReactDOM from 'react-dom';
import classNames from 'classnames';

import { Scrollbars } from 'react-custom-scrollbars';

import { ReactComponent as Cross } from 'shared/components/Images/ui/cross.svg';
import { rootElement } from 'utils';

import styles from './modal.module.scss';

import { TModalProps } from './Modal.types';
import SpinWrapped from 'shared/components/SpinWrapped';
import Process from 'modules/EmployeeApp/components/Evaluation/Constants/Process';
import TButton from '../Button/Button';

const _modal = (
  setOpen: Dispatch<SSA<boolean>>,
  setLoaded: Dispatch<SSA<boolean>>,
  isLoad: boolean,
  children: React.ReactNode,
  isPopup: boolean,
  isReadOnly: boolean,
  onClose: () => void,
  process: Process,
  { ...props }: TModalProps
): JSX.Element => {
  const closeModal: (e?: RMouseEvent<SVGSVGElement | HTMLButtonElement, MouseEvent>) => void
    = (e?: RMouseEvent<SVGSVGElement | HTMLButtonElement, MouseEvent>): void => {
      if (e) {
        e.preventDefault();
        e.stopPropagation();
      }

      setOpen(() => false);
      onClose();
    };

  const handleConfirm: () => void = (): void => {
    setLoaded(() => true);
    props.handleConfirm()
      .then((): void => {
        if (props.initAfterConfirm) {
          props.initAfterConfirm();
        }
      })
      .catch((): void => {
        return setLoaded(() => false);
      });
  };

  const handleDecline = (e?: React.MouseEvent<SVGSVGElement | HTMLButtonElement, MouseEvent> | undefined): void => {
    if (props.handleDecline) {
      props.handleDecline(true);
    }
    closeModal(e);
  };

  const handleOutside = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    closeModal();
    onClose();

    e.stopPropagation();
  };

  const handleInside = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    if (isReadOnly) {
      e.preventDefault();
    }

    e.stopPropagation();
  };

  const content = (
    <>
      {
        !isPopup && (
          <div className={styles.frame__header} style={{ textAlign: props.style?.textAlign }}>
            <span>{props.title ?? ''}</span>
            <Cross onClick={closeModal} style={{ cursor: 'pointer' }} />
          </div>
        )
      }

      <div className={styles.frame__content}>
        {children}
      </div>

      {
        !isPopup && !isReadOnly && (
          <div className={styles.frame__buttonGroup} style={{ justifyContent: props.style?.justifyContent, padding: props.style?.padding }}>
            {!props.declineButton?.hide
            && (
            <TButton
              onClick={handleDecline as any}
              className={styles.frame__cancelButton}
            >
              {props.declineButton?.text ?? 'Отменить'}
            </TButton>
            )}

            {props.confirmButton && (!isLoad
              ? (
                <button
                  onClick={handleConfirm}
                  style={{
                    width: props.style?.width,
                    margin: props.style?.margin,
                  }}
                  type="submit"
                  className={styles.frame__confirmButton}
                >
                  {props.confirmButton.text}
                </button>
              )
              : <SpinWrapped className={styles.spin} />
            )}
          </div>
        )
      }
    </>
  );

  return (
    <div className={styles.modalContainer} onClick={handleOutside}>
      <div
        className={classNames(styles.frame, isPopup && styles.frame__poppup, (process !== Process.START || isReadOnly) && styles.frame__isReadOnly)}
        onClick={handleInside}
        style={{ height: props.style?.height }}
      >
        {
            (process === Process.START && !isReadOnly && !props.withoutScrolls)
              ? (
                <Scrollbars style={{ width: '526px' }}>
                  {content}
                </Scrollbars>
              )
              : content
          }
      </div>
    </div>
  );
};

/**
 * Component is implemented from SberTransport-Fleet project.
 *
 * @param setOpen - callBack for opening and closing of modal
 * @param isPoppup - make modal without Title & close icon
 * @returns ReactPortal or null
 */

const TModal = ({
  properties,
  children,
  isPoppup,
  isReadOnly,
  process,
  onClose,
}: {
  properties: TModalProps;
  children: React.ReactNode;
  isPoppup: boolean;
  isReadOnly: boolean;
  process: Process;
  onClose: () => void;
}): ReactPortal | null => {
  const [isOpen, setOpen] = useState(false);
  const [isLoad, setLoaded] = useState(false);

  useEffect(() => {
    if (process === Process.START) {
      setOpen(() => true);
    } else if (process === Process.CLOSE) {
      setOpen(() => false);
      setLoaded(() => false);
    }
  }, [properties]);

  return isOpen
    ? ReactDOM.createPortal(
      _modal(
        setOpen,
        setLoaded,
        isLoad,
        children,
        isPoppup,
        isReadOnly,
        onClose,
        process,
        properties
      ),
      rootElement()
    ) : null;
};

export {
  TModal
};
