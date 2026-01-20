using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SafeDalat_API.Migrations
{
    /// <inheritdoc />
    public partial class udtrustuserscoretableuser : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "TrustScore",
                table: "Users",
                type: "int",
                nullable: false,
                defaultValue: 0);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "TrustScore",
                table: "Users");
        }
    }
}
