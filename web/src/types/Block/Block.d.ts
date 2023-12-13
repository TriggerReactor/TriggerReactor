interface Executor {
    name: string,
    args: BlockDataType[]
}

interface BlockProps {
    name: string,
    args: BlockDataType[]
}

type BlockDataType = {
    placeholder: string,
    currentClass: string,
    inputedArgs: BlockProps | null
}