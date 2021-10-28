const fs = require("fs");
const sourceRaw = fs.readFileSync(__dirname+"/report.json");
const sources = JSON.parse(sourceRaw);
const output = {
    $schema:
        "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Documents/CommitteeSpecifications/2.1.0/sarif-schema-2.1.0.json",
    version: "2.1.0",
    runs: [],
};
sources.forEach((source) => {
    output.runs.push({
        tool: {
            driver: {
                name: source.tool,
                rules: [
                    {
                        id: source.hash.toString(),
                        shortDescription: {
                            text: source.comment,
                        },
                        fullDescription: {
                            text: source.type,
                        },
                        help: {
                            text: source.detailsInfo,
                            markdown: "",
                        },
                        properties: {
                            tags: [source.category, "qodana", ...source.tags],
                        },
                    },
                ],
            },
        },
        results: [
            {
                ruleId: source.hash.toString(),
                level: "warning",
                message: {
                    text: source.attributes.inspectionName,
                },
                locations: [],
            },
        ],
    });
});
try {
    fs.writeFileSync(__dirname+'/report.sarif', JSON.stringify(output, null, 2))
} catch (err) {
    console.log(err)
}
