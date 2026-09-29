import React, { FC } from "react";
import { FormInstance } from "antd/es/form";
import SpinWrapped from "shared/components/SpinWrapped/SpinWrapped";
import { Form } from "../Form/Form";
import { RouteInformation } from "../RouteInformation";
import { RouteType } from "../../types";

interface Props {
  form?: FormInstance,
  route: RouteType;
  updateRoute: (route: RouteType | { tariffId?: string }) => void;
  isLoading: boolean;
  distance: number;
  volume: number;
  weight: number;
  cost: number;
}

export const MainInfo: FC<Props> = (props) => {

  const { form, route, distance, volume, weight, cost, updateRoute,
    isLoading } = props;

  if (isLoading) {
    return <SpinWrapped />;
  }

  return(
    <>
      <Form
        form={form}
        route={route}
        updateRoute={updateRoute}
      />
      <RouteInformation
        distance={distance}
        volume={volume}
        weight={weight}
        cost={cost}
      />
    </>
  )
};
