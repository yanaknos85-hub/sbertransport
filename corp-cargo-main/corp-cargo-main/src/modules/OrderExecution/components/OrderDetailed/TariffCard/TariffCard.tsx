import { Card } from "antd";
import React from "react";
import cn from "classnames";
import styles from "./styles.module.scss";

const TariffCard = ({ contragent, transportType, autoName }) => {
  return (
    <Card className={styles.tariffCard}>
      <h3>{contragent}</h3>
      <p>{transportType}</p>
      <p>{autoName}</p>
    </Card>
  );
};

export const TariffSection = ({ tariffs, tariffId, setTariffId }) => {
  return (
    <div className={styles.container}>
      {tariffs.map((tariff) => (
        <div
          className={cn({
            [styles.active]: tariffId === tariff.id,
            [styles.row]: true,
          })}
          onClick={() => setTariffId(tariff.id)}
          key={tariff.id}
        >
          <TariffCard
            contragent={tariff.contragent}
            transportType={tariff.transportType?.nameRus}
            autoName={tariff.auto?.name}
          />
        </div>
      ))}
    </div>
  );
};
