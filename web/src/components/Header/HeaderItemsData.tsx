export const imgRoot = "./imgs/icons/headers/";

export const headerItems: HeaderItemProps[] = [
    { name: "File", imgPath:(imgRoot + "file.svg"), onClick: () => {}, isIconOnly: false },
    { name: "Edit", imgPath:(imgRoot + "edit.svg"), onClick: () => {}, isIconOnly: false },
    { name: "Selection", imgPath:(imgRoot + "selection.svg"), onClick: () => {}, isIconOnly: false },
    { name: "Libraries", imgPath:(imgRoot + "libraries.svg"), onClick: () => {}, isIconOnly: false },
    { name: "Help", imgPath:(imgRoot + "help.svg"), onClick: () => {}, isIconOnly: false },

    { name: "run", imgPath:(imgRoot + "run.svg"), onClick: () => {}, isIconOnly: true },
    { name: "debug", imgPath:(imgRoot + "debug.svg"), onClick: () => {}, isIconOnly: true },
    { name: "terminal", imgPath:(imgRoot + "open-terminal.svg"), onClick: () => {}, isIconOnly: true },
    { name: "show-code", imgPath:(imgRoot + "show-code.svg"), onClick: () => {}, isIconOnly: true },
];