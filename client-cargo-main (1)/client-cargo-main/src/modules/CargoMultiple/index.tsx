import React, { FC } from 'react';
import { observer } from 'mobx-react';

import CreateCargo from './CargoMultiple';

const CreateMultipleCargoRequest: FC = observer(() => <CreateCargo />);

export { CreateMultipleCargoRequest };
